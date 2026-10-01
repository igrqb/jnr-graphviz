import io.github.igrqb.jnr.graphviz.Agdesc;
import io.github.igrqb.jnr.graphviz.AgobjKind;
import io.github.igrqb.jnr.graphviz.LibC;
import io.github.igrqb.jnr.graphviz.LibCgraph;
import io.github.igrqb.jnr.graphviz.LibGvc;
import io.github.igrqb.jnr.graphviz.LibPack;
import io.github.igrqb.jnr.graphviz.NativeGraphviz;
import jnr.ffi.Memory;
import jnr.ffi.Pointer;

import java.io.IOException;

/**
 * Java ports of the Graphviz C examples in {@code src/test/c}.
 * Each method makes the same library calls as the program it is named for.
 */
public final class ExamplePorts {
  private static final LibC libc = NativeGraphviz.libc;
  private static final LibCgraph cgraph = NativeGraphviz.cgraph;
  private static final LibGvc gvc = NativeGraphviz.gvc;
  private static final LibPack pack = NativeGraphviz.pack;

  private ExamplePorts() {
  }

  /** {@code dot_example.c}, which is upstream {@code dot.demo/example.c} with layout left out. */
  public static byte[] example() {
    Pointer graph = cgraph.agopen("g", Agdesc.directed(), null);
    try {
      Pointer n = cgraph.agnode(graph, "n", 1);
      Pointer m = cgraph.agnode(graph, "m", 1);
      cgraph.agedge(graph, n, m, null, 1);
      cgraph.agsafeset(n, "color", "red", "");
      try (NativeGraphviz.MemStream stream = new NativeGraphviz.MemStream()) {
        cgraph.agwrite(graph, stream.file());
        return stream.toByteArray();
      }
    } finally {
      cgraph.agclose(graph);
    }
  }

  /** {@code dot_simple.c}, upstream {@code dot.demo/simple.c}. */
  public static byte[] simple(String path) {
    Pointer context = gvc.gvContext();
    Pointer graph = readFile(path);
    try {
      gvc.gvLayout(context, graph, "dot");
      try (NativeGraphviz.MemStream stream = new NativeGraphviz.MemStream()) {
        gvc.gvRender(context, graph, "plain", stream.file());
        return stream.toByteArray();
      }
    } finally {
      gvc.gvFreeLayout(context, graph);
      cgraph.agclose(graph);
      gvc.gvFreeContext(context);
    }
  }

  /**
   * {@code dot_demo.c}, upstream {@code dot.demo/demo.c}.
   * {@code argv0} is the C executable path, so {@code gvParseArgs} sees the same argv[0].
   */
  public static byte[] demo(String argv0) throws IOException {
    return NativeGraphviz.captureStdout(() -> {
      Pointer context = gvc.gvContext();
      NativeGraphviz.Args args = NativeGraphviz.args(argv0, "-Kdot", "-Tplain");
      gvc.gvParseArgs(context, args.argc, args.argv);
      Pointer graph = cgraph.agopen("g", Agdesc.directed(), null);
      Pointer n = cgraph.agnode(graph, "n", 1);
      Pointer m = cgraph.agnode(graph, "m", 1);
      cgraph.agedge(graph, n, m, null, 1);
      cgraph.agsafeset(n, "color", "red", "");
      gvc.gvLayoutJobs(context, graph);
      gvc.gvRenderJobs(context, graph);
      gvc.gvFreeLayout(context, graph);
      cgraph.agclose(graph);
      gvc.gvFreeContext(context);
    });
  }

  /** {@code dot_input.c}, upstream {@code dot.demo/dot.c}, reading {@code path}. */
  public static byte[] input(String argv0, String path) throws IOException {
    return NativeGraphviz.captureStdout(() -> {
      Pointer context = gvc.gvContext();
      NativeGraphviz.Args args = NativeGraphviz.args(argv0, "-Kdot", "-Tplain", path);
      gvc.gvParseArgs(context, args.argc, args.argv);
      Pointer prev = null;
      Pointer graph;
      while (alive(graph = gvc.gvNextInputGraph(context))) {
        if (alive(prev)) {
          gvc.gvFreeLayout(context, prev);
          cgraph.agclose(prev);
        }
        gvc.gvLayoutJobs(context, graph);
        gvc.gvRenderJobs(context, graph);
        prev = graph;
      }
      gvc.gvFreeContext(context);
    });
  }

  /** {@code dot_render_context.c}. A null context renders to stdout. */
  public static byte[] renderContext() throws IOException {
    return NativeGraphviz.captureStdout(() -> {
      Pointer context = gvc.gvContext();
      Pointer graph = cgraph.agmemread("digraph { a -> b }");
      gvc.gvLayout(context, graph, "dot");
      gvc.gvRenderContext(context, graph, "svg", null);
      gvc.gvFreeLayout(context, graph);
      cgraph.agclose(graph);
      gvc.gvFinalize(context);
      gvc.gvFreeContext(context);
    });
  }

  /**
   * {@code dot_neatopack.c}, upstream {@code dot.demo/neatopack.c} adapted to Graphviz 2.42.
   * Component layouts are released with {@code agdelete}. {@code gvFreeLayout} aborts after {@code pack_graph}.
   */
  public static byte[] neatopack(String path, GraphvizC.Sizes sizes) {
    Pointer context = gvc.gvContext();
    Pointer graph = readFile(path);
    Pointer components = null;
    try {
      cgraph.aginit(graph, AgobjKind.GRAPH, "Agraphinfo_t", sizes.agraphinfo, 1);
      cgraph.aginit(graph, AgobjKind.NODE, "Agnodeinfo_t", sizes.agnodeinfo, 1);
      Pointer count = Memory.allocateDirect(NativeGraphviz.runtime, Integer.BYTES);
      count.putInt(0, 0);
      components = pack.ccomps(graph, count, null);
      int ncc = count.getInt(0);
      for (int i = 0; i < ncc; i++) {
        Pointer component = components.getPointer(i * 8L);
        pack.nodeInduce(component);
        gvc.gvLayout(context, component, "neato");
      }
      pack.pack_graph(ncc, components, graph, null);
      try (NativeGraphviz.MemStream stream = new NativeGraphviz.MemStream()) {
        gvc.gvRender(context, graph, "ps", stream.file());
        byte[] bytes = stream.toByteArray();
        for (int i = 0; i < ncc; i++) {
          cgraph.agdelete(graph, components.getPointer(i * 8L));
        }
        return bytes;
      }
    } finally {
      if (alive(components)) {
        libc.free(components);
      }
      cgraph.agclose(graph);
      gvc.gvFreeContext(context);
    }
  }

  private static Pointer readFile(String path) {
    Pointer file = libc.fopen(path, "r");
    if (!alive(file)) {
      throw new IllegalStateException("fopen " + path);
    }
    try {
      Pointer graph = cgraph.agread(file, null);
      if (!alive(graph)) {
        throw new IllegalStateException("agread " + path);
      }
      return graph;
    } finally {
      libc.fclose(file);
    }
  }

  private static boolean alive(Pointer pointer) {
    return pointer != null && pointer.address() != 0;
  }
}
