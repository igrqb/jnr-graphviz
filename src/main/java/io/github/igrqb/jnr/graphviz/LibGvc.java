package io.github.igrqb.jnr.graphviz;

import jnr.ffi.Pointer;

/**
 * JNR bindings for libgvc, the Graphviz context library declared in {@code gvc.h}.
 * <p>
 * Layout and rendering use a context from {@link #gvContext()} and a graph from libcgraph.
 * See <a href="https://graphviz.org/docs/library/">Using Graphviz as a library</a>.
 */
public interface LibGvc {

  /**
   * Set up a graphviz context (libgvc).
   * See <a href="https://graphviz.org/pdf/gvc.3.pdf">libgvc − io.github.igrqb.jnr.graphviz.Graphviz context library</a>
   * @return pointer to graphviz context (type GVC_t)
   */
  Pointer gvContext();

  /**
   * agmemread attempts to read a graph from the input string.
   * See <a href="https://graphviz.org/pdf/cgraph.3.pdf">libcgraph − abstract graph library</a>
   * @param dotText input graph in dot language
   * @return pointer of type Agraph_t
   */
  Pointer agmemread(String dotText);

  /**
   * Compute a layout using a specified engine
   * See <a href="https://graphviz.org/pdf/gvc.3.pdf">libgvc − io.github.igrqb.jnr.graphviz.Graphviz context library</a>
   * @param gvc *GVC_t
   * @param g *graph_t
   * @param engine name of engine to use, e.g. "dot". More engines here: <a href="https://graphviz.org/docs/layouts/">Layout Engines</a>
   * @return 0 on success, non-zero on error
   */
  int gvLayout(Pointer gvc, Pointer g, String engine);

  /**
   * Render layout in a specified format to an open FILE.
   * See <a href="https://graphviz.org/pdf/gvc.3.pdf">libgvc − io.github.igrqb.jnr.graphviz.Graphviz context library</a>
   * @param gvc *GVC_t
   * @param g *graph_t
   * @param format output format, e.g. "svg"
   * @param out output stream: file, stdout, open_memstream
   * @return 0 on success, non-zero on error
   */
  int gvRender(Pointer gvc, Pointer g, String format, Pointer out);

  /**
   * Clean up layout data structures - layouts are not nestable (yet)
   * See <a href="https://graphviz.org/pdf/gvc.3.pdf">libgvc − io.github.igrqb.jnr.graphviz.Graphviz context library</a>
   * @param gvc *GVC_t
   * @param g *graph_t
   * @return 0 on success, non-zero on error
   */
  int gvFreeLayout(Pointer gvc, Pointer g);

  /**
   * agclose deletes a graph, freeing its associated storage.
   * See <a href="https://graphviz.org/pdf/cgraph.3.pdf">libcgraph − abstract graph library</a>
   * @param g *Agraph_t
   * @return 0 on success, non-zero on error
   */
  int agclose(Pointer g);


  /**
   * Clean up graphviz context
   * @param gvc *GVC_t
   * @return 0 on success, non-zero on error
   */
  int gvFreeContext(Pointer gvc);

  /** Toggle an internal debugging switch. Retained from {@code gvc.h}. */
  void gvToggle(int value);

  /**
   * Create a context from an explicit builtin symbol list.
   * @param builtins {@code lt_symlist_t *}, or NULL
   * @param demandLoading non-zero to load plugins on demand
   */
  Pointer gvNEWcontext(Pointer builtins, int demandLoading);

  /**
   * Create a context that uses the given builtins.
   * @param builtins {@code lt_symlist_t *}, or NULL
   * @param demandLoading non-zero to load plugins on demand
   */
  Pointer gvContextPlugins(Pointer builtins, int demandLoading);

  /**
   * Package name, version, and build date. The array has three strings and is not freed by the caller.
   * @return {@code char **}
   */
  Pointer gvcInfo(Pointer gvc);

  /** Graphviz version string for this context. */
  String gvcVersion(Pointer gvc);

  /** Build date string for this context. */
  String gvcBuildDate(Pointer gvc);

  /**
   * Parse command-line arguments. {@code argv[0]} selects the layout engine unless {@code -K} is set.
   * @param argv {@code char **} of {@code argc} strings
   * @return 0 on success
   */
  int gvParseArgs(Pointer gvc, int argc, Pointer argv);

  /** Next input graph named by {@link #gvParseArgs}, or NULL. */
  Pointer gvNextInputGraph(Pointer gvc);

  /** Graph that describes the loaded plugins, or NULL. */
  Pointer gvPluginsGraph(Pointer gvc);

  /** Lay out {@code g} with the engine selected by {@link #gvParseArgs}. Returns 0 on success. */
  int gvLayoutJobs(Pointer gvc, Pointer g);

  /** Copy layout coordinates into string attributes of {@code g}. */
  void attach_attrs(Pointer g);

  /** Render to a file path. Returns 0 on success. */
  int gvRenderFilename(Pointer gvc, Pointer g, String format, String filename);

  /**
   * Render through an external device context.
   * A NULL context renders to the C stdout for the usual formats.
   */
  int gvRenderContext(Pointer gvc, Pointer g, String format, Pointer context);

  /**
   * Render into a newly allocated buffer.
   * @param result pointer to a {@code char *} that receives the buffer
   * @param length pointer to an {@code unsigned int} that receives the byte count
   * @return 0 on success
   */
  int gvRenderData(Pointer gvc, Pointer g, String format, Pointer result, Pointer length);

  /** Free the buffer returned through {@link #gvRenderData}. */
  void gvFreeRenderData(Pointer data);

  /** Render using the {@code -T} and {@code -o} options from {@link #gvParseArgs}. Returns 0 on success. */
  int gvRenderJobs(Pointer gvc, Pointer g);

  /** Finish any active render job. */
  void gvFinalize(Pointer gvc);

  /**
   * Names of plugins of {@code kind} ({@code "render"}, {@code "layout"}, {@code "textlayout"},
   * {@code "device"}, {@code "loadimage"}).
   * @param count pointer to an {@code int} that receives the length
   * @param ignored unused, as in the 2.42 {@code gvc.h} declaration
   * @return a malloc'd list of malloc'd strings. Free each string, then the list.
   */
  Pointer gvPluginList(Pointer gvc, String kind, Pointer count, String ignored);

  /** Register a plugin library built by the caller. {@code library} is a {@code gvplugin_library_t *}. */
  void gvAddLibrary(Pointer gvc, Pointer library);

  /** Transitive reduction. Returns 0 on success. */
  int gvToolTred(Pointer g);

  /**
   * Shallow copy of a context. Declared by later {@code gvc.h} and exported by libgvc 2.42.
   * Free the result with {@link #gvFreeCloneGVC}, not {@link #gvFreeContext}.
   */
  Pointer gvCloneGVC(Pointer gvc);

  /** Free a context created by {@link #gvCloneGVC}. */
  void gvFreeCloneGVC(Pointer gvc);
}
