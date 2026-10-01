import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Compiles the C oracles under {@code src/test/c} and runs them against the same
 * libgvc and libcgraph the Java bindings call.
 */
public final class GraphvizC {
  private static final Path ROOT = Path.of("").toAbsolutePath();
  private static final Path SOURCE = ROOT.resolve("src/test/c");
  private static final Path OUT = ROOT.resolve("build/c-examples");

  private GraphvizC() {
  }

  /** Sizes of the C structs the oracle passes through. */
  public static final class Sizes {
    public final int agraphinfo;
    public final int agnodeinfo;
    public final int agedgeinfo;
    public final int agrec;
    public final int packInfo;

    Sizes(int agraphinfo, int agnodeinfo, int agedgeinfo, int agrec, int packInfo) {
      this.agraphinfo = agraphinfo;
      this.agnodeinfo = agnodeinfo;
      this.agedgeinfo = agedgeinfo;
      this.agrec = agrec;
      this.packInfo = packInfo;
    }
  }

  /** Compile {@code src/test/c/name.c} to {@code build/c-examples/name}. */
  public static Path compile(String name) throws IOException, InterruptedException {
    if (!Files.isDirectory(SOURCE)) {
      throw new IllegalStateException("C sources not found at " + SOURCE + " (cwd " + ROOT + ")");
    }
    Files.createDirectories(OUT);
    Path src = SOURCE.resolve(name + ".c");
    Path exe = OUT.resolve(name);
    List<String> command = List.of(
      "gcc", "-O0", "-Wall", "-o", exe.toString(), src.toString(),
      "-lgvc", "-lcgraph", "-lm");
    runCapture(command);
    if (!Files.isExecutable(exe)) {
      throw new IllegalStateException("gcc did not write " + exe);
    }
    return exe;
  }

  /** {@code sizeof} of the records used by the packing and callback examples. */
  public static Sizes sizes() throws IOException, InterruptedException {
    Path exe = compile("sizes");
    String text = new String(run(exe), StandardCharsets.UTF_8);
    Map<String, Integer> values = new java.util.HashMap<>();
    for (String line : text.split("\n")) {
      if (line.isBlank()) {
        continue;
      }
      String[] parts = line.split(" ");
      values.put(parts[0], Integer.parseInt(parts[1]));
    }
    return new Sizes(
      values.get("Agraphinfo_t"),
      values.get("Agnodeinfo_t"),
      values.get("Agedgeinfo_t"),
      values.get("Agrec_t"),
      values.get("pack_info"));
  }

  /**
   * Run {@code exe} and return its stdout.
   * {@code args} are the arguments after argv[0]. argv[0] is the executable path.
   */
  public static byte[] run(Path exe, String... args) throws IOException, InterruptedException {
    List<String> command = new ArrayList<>();
    command.add(exe.toString());
    command.addAll(List.of(args));
    Path err = Files.createTempFile("c-example-", ".err");
    ProcessBuilder builder = new ProcessBuilder(command);
    builder.redirectError(err.toFile());
    Process process = builder.start();
    byte[] out = process.getInputStream().readAllBytes();
    int code = process.waitFor();
    String errors = Files.readString(err);
    Files.deleteIfExists(err);
    if (code != 0) {
      throw new IllegalStateException(exe.getFileName() + " exited " + code + "\n" + errors);
    }
    return out;
  }

  private static void runCapture(List<String> command) throws IOException, InterruptedException {
    Path err = Files.createTempFile("c-example-", ".err");
    ProcessBuilder builder = new ProcessBuilder(command);
    builder.redirectErrorStream(true);
    builder.redirectOutput(err.toFile());
    Process process = builder.start();
    int code = process.waitFor();
    String log = Files.readString(err);
    Files.deleteIfExists(err);
    if (code != 0) {
      throw new IllegalStateException(String.join(" ", command) + " exited " + code + "\n" + log);
    }
  }
}
