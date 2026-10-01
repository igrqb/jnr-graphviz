import io.github.igrqb.jnr.graphviz.Agdesc;
import io.github.igrqb.jnr.graphviz.LibCgraph;
import io.github.igrqb.jnr.graphviz.NativeGraphviz;
import jnr.ffi.Pointer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Compiles the Graphviz C examples and {@code api_oracle.c}, runs them, and compares
 * their stdout with the same calls made through the JNR bindings.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestCExamples {
  private static GraphvizC.Sizes sizes;
  private static Path example;
  private static Path simple;
  private static Path demo;
  private static Path input;
  private static Path renderContext;
  private static Path neatopack;
  private static Path oracle;
  private static Path simpleGv;
  private static Path multiGv;
  private static Path packGv;

  @BeforeAll
  static void compileOracles() throws Exception {
    sizes = GraphvizC.sizes();
    example = GraphvizC.compile("dot_example");
    simple = GraphvizC.compile("dot_simple");
    demo = GraphvizC.compile("dot_demo");
    input = GraphvizC.compile("dot_input");
    renderContext = GraphvizC.compile("dot_render_context");
    neatopack = GraphvizC.compile("dot_neatopack");
    oracle = GraphvizC.compile("api_oracle");
    Path root = Path.of("").toAbsolutePath();
    simpleGv = root.resolve("src/test/resources/simple.gv");
    multiGv = root.resolve("src/test/resources/multi.gv");
    packGv = root.resolve("src/test/resources/pack.gv");
  }

  @Test
  @Order(1)
  public void testDirectedDescriptorOpensADirectedGraph() {
    Assertions.assertEquals(9, Agdesc.directed());
    Assertions.assertEquals(11, Agdesc.strictDirected());
    Assertions.assertEquals(8, Agdesc.undirected());
    Assertions.assertEquals(10, Agdesc.strictUndirected());
    LibCgraph cgraph = NativeGraphviz.cgraph;
    Pointer graph = cgraph.agopen("g", Agdesc.directed(), null);
    try {
      Assertions.assertNotEquals(0, graph.address());
      Assertions.assertEquals(1, cgraph.agisdirected(graph));
      Assertions.assertEquals(0, cgraph.agisundirected(graph));
    } finally {
      cgraph.agclose(graph);
    }
  }

  @Test
  @Order(2)
  public void testExampleMatchesC() throws Exception {
    assertSameOutput("example", GraphvizC.run(example), ExamplePorts.example());
  }

  @Test
  @Order(3)
  public void testSimpleMatchesC() throws Exception {
    String path = simpleGv.toString();
    assertSameOutput("simple", GraphvizC.run(simple, path), ExamplePorts.simple(path));
  }

  @Test
  @Order(4)
  public void testDemoMatchesC() throws Exception {
    assertSameOutput("demo",
      GraphvizC.run(demo, "-Kdot", "-Tplain"),
      ExamplePorts.demo(demo.toString()));
  }

  @Test
  @Order(5)
  public void testInputMatchesC() throws Exception {
    String path = multiGv.toString();
    assertSameOutput("input",
      GraphvizC.run(input, "-Kdot", "-Tplain", path),
      ExamplePorts.input(input.toString(), path));
  }

  @Test
  @Order(6)
  public void testRenderContextMatchesC() throws Exception {
    assertSameOutput("render-context", GraphvizC.run(renderContext), ExamplePorts.renderContext());
  }

  @Test
  @Order(7)
  public void testNeatopackMatchesC() throws Exception {
    String path = packGv.toString();
    assertSameOutput("neatopack", GraphvizC.run(neatopack, path), ExamplePorts.neatopack(path, sizes));
  }

  @Test
  @Order(8)
  public void testApiMatchesC() throws Exception {
    assertSameOutput("api", GraphvizC.run(oracle), forkOracle());
  }

  /**
   * The oracle names an anonymous edge from a counter cgraph keeps for the whole process.
   * Other tests in this JVM move that counter, so the Java side runs in its own process.
   */
  private static byte[] forkOracle() throws Exception {
    String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
    ProcessBuilder builder = new ProcessBuilder(
      java, "-cp", System.getProperty("java.class.path"), "ApiOracle");
    builder.redirectError(ProcessBuilder.Redirect.INHERIT);
    Process process = builder.start();
    byte[] out = process.getInputStream().readAllBytes();
    int code = process.waitFor();
    if (code != 0) {
      throw new IllegalStateException("ApiOracle exited " + code);
    }
    return out;
  }

  private static void assertSameOutput(String label, byte[] expected, byte[] actual) {
    if (java.util.Arrays.equals(expected, actual)) {
      return;
    }
    int shared = Math.min(expected.length, actual.length);
    int index = 0;
    while (index < shared && expected[index] == actual[index]) {
      index++;
    }
    Assertions.fail(label + " differs at byte " + index
      + " (expected length " + expected.length + ", actual length " + actual.length + ")\n"
      + "expected: " + window(expected, index) + "\n"
      + "actual:   " + window(actual, index));
  }

  private static String window(byte[] bytes, int index) {
    if (bytes.length == 0) {
      return "<empty>";
    }
    int from = Math.max(0, index - 40);
    int to = Math.min(bytes.length, index + 120);
    String text = new String(bytes, from, to - from, StandardCharsets.ISO_8859_1);
    return "offset " + from + " [" + text.replace("\n", "\\n") + "]";
  }
}
