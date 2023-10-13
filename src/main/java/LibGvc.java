import jnr.ffi.Pointer;

/**
 * JNR interface to libgvc - Graphviz context library. Exposes a subset of function required for conversion from dot to
 * svg.
 * For more information please refer to the Graphviz documentation:
 * <a href="https://graphviz.org/docs/library/">Using Graphviz as a library</a>
 */
public interface LibGvc {

  /**
   * Set up a graphviz context (libgvc).
   * See <a href="https://graphviz.org/pdf/gvc.3.pdf">libgvc − Graphviz context library</a>
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
   * See <a href="https://graphviz.org/pdf/gvc.3.pdf">libgvc − Graphviz context library</a>
   * @param gvc *GVC_t
   * @param g *graph_t
   * @param engine name of engine to use, e.g. "dot". More engines here: <a href="https://graphviz.org/docs/layouts/">Layout Engines</a>
   * @return 0 on success, non-zero on error
   */
  int gvLayout(Pointer gvc, Pointer g, String engine);

  /**
   * Render layout in a specified format to an open FILE.
   * See <a href="https://graphviz.org/pdf/gvc.3.pdf">libgvc − Graphviz context library</a>
   * @param gvc *GVC_t
   * @param g *graph_t
   * @param format output format, e.g. "svg"
   * @param out output stream: file, stdout, open_memstream
   * @return 0 on success, non-zero on error
   */
  int gvRender(Pointer gvc, Pointer g, String format, Pointer out);

  /**
   * Clean up layout data structures - layouts are not nestable (yet)
   * See <a href="https://graphviz.org/pdf/gvc.3.pdf">libgvc − Graphviz context library</a>
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
}
