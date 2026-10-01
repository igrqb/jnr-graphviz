package io.github.igrqb.jnr.graphviz;

import jnr.ffi.Pointer;
import jnr.ffi.Variable;
import jnr.ffi.annotations.Variadic;
import jnr.ffi.types.size_t;
import jnr.ffi.types.u_int64_t;

/**
 * JNR bindings for libcgraph, the Graphviz graph library declared in {@code cgraph.h}.
 * <p>
 * Pointer arguments are the C object pointers ({@code Agraph_t *}, {@code Agnode_t *},
 * {@code Agedge_t *}, {@code Agsym_t *}, {@code FILE *}). A null pointer is a C NULL.
 * Strings the library stores, such as {@link #agsetfile(Pointer)}, must stay allocated
 * for as long as cgraph uses them. Strings cgraph copies ({@code agopen}, {@code agnode},
 * {@code agsafeset}) can be ordinary Java strings.
 * <p>
 * {@code Agdesc_t} is a 4-byte bitfield passed by value. Pass the value of
 * {@link #Agdirected()} or the flags in {@link Agdesc}.
 * <p>
 * See <a href="https://graphviz.org/pdf/cgraph.3.pdf">libcgraph</a>.
 */
public interface LibCgraph {

  /** Graph descriptor for a directed graph. The value is a 4-byte {@code Agdesc_t}. */
  Variable<Integer> Agdirected();

  /** Graph descriptor for a strict directed graph. */
  Variable<Integer> Agstrictdirected();

  /** Graph descriptor for an undirected graph. */
  Variable<Integer> Agundirected();

  /** Graph descriptor for a strict undirected graph. */
  Variable<Integer> Agstrictundirected();

  /** Push an object-event callback. {@code disc} is an {@code Agcbdisc_t *}. */
  void agpushdisc(Pointer g, Pointer disc, Pointer state);

  /** Pop a callback pushed with {@link #agpushdisc}. Returns 0 on success. */
  int agpopdisc(Pointer g, Pointer disc);

  /**
   * Enable or disable callbacks.
   * @param flag non-zero to enable
   * @return the previous flag
   */
  int agcallbacks(Pointer g, int flag);

  /**
   * Create a top-level graph.
   * @param name graph name, copied by cgraph
   * @param desc {@code Agdesc_t} by value, as a 32-bit bitfield
   * @param disc {@code Agdisc_t *}, or NULL for the default discipline
   * @return the new graph
   */
  Pointer agopen(String name, int desc, Pointer disc);

  /** Delete a graph and its storage. Returns 0 on success. */
  int agclose(Pointer g);

  /** Read a graph from a {@code FILE *}. {@code disc} may be NULL. */
  Pointer agread(Pointer channel, Pointer disc);

  /** Read one graph from a DOT string. */
  Pointer agmemread(String dot);

  /** Append the graph in {@code dot} to {@code g}. */
  Pointer agmemconcat(Pointer g, String dot);

  /** Set the line number reported for the next parse error. */
  void agreadline(int lineNumber);

  /**
   * Set the file name reported for the next parse error.
   * cgraph keeps this pointer, so {@code name} must outlive the next read.
   */
  void agsetfile(Pointer name);

  /** Read another graph from {@code channel} and merge it into {@code g}. */
  Pointer agconcat(Pointer g, Pointer channel, Pointer disc);

  /** Write {@code g} as DOT to a {@code FILE *}. Returns 0 on success. */
  int agwrite(Pointer g, Pointer channel);

  /** Non-zero when the graph is directed. */
  int agisdirected(Pointer g);

  /** Non-zero when the graph is undirected. */
  int agisundirected(Pointer g);

  /** Non-zero when multi-edges are forbidden. */
  int agisstrict(Pointer g);

  /** Non-zero when the graph has no loops and no multi-edges. */
  int agissimple(Pointer g);

  /**
   * Find or create a node.
   * @param createflag non-zero to create the node when it is missing
   */
  Pointer agnode(Pointer g, String name, int createflag);

  /** Find or create a node by its integer id. */
  Pointer agidnode(Pointer g, @u_int64_t long id, int createflag);

  /** Find or insert {@code node} in {@code g}, which may be a subgraph. */
  Pointer agsubnode(Pointer g, Pointer node, int createflag);

  /** First node in sequence order, or NULL. */
  Pointer agfstnode(Pointer g);

  /** Node after {@code n}, or NULL. */
  Pointer agnxtnode(Pointer g, Pointer n);

  /** Last node in sequence order, or NULL. */
  Pointer aglstnode(Pointer g);

  /** Node before {@code n}, or NULL. */
  Pointer agprvnode(Pointer g, Pointer n);

  /** Per-graph node record for {@code n}, or NULL. */
  Pointer agsubrep(Pointer g, Pointer n);

  /** Move node {@code u} before node {@code v} in the sequence. Returns 0 on success. */
  int agnodebefore(Pointer u, Pointer v);

  /**
   * Find or create an edge.
   * @param name edge name, or NULL
   * @param createflag non-zero to create the edge when it is missing
   */
  Pointer agedge(Pointer g, Pointer tail, Pointer head, String name, int createflag);

  /** Find or create an edge by its integer id. */
  Pointer agidedge(Pointer g, Pointer tail, Pointer head, @u_int64_t long id, int createflag);

  /** Find or insert {@code edge} in {@code g}. */
  Pointer agsubedge(Pointer g, Pointer edge, int createflag);

  /** First in-edge of {@code n}, or NULL. */
  Pointer agfstin(Pointer g, Pointer n);

  /** In-edge after {@code e}, or NULL. */
  Pointer agnxtin(Pointer g, Pointer e);

  /** First out-edge of {@code n}, or NULL. */
  Pointer agfstout(Pointer g, Pointer n);

  /** Out-edge after {@code e}, or NULL. */
  Pointer agnxtout(Pointer g, Pointer e);

  /** First in- or out-edge of {@code n}, or NULL. */
  Pointer agfstedge(Pointer g, Pointer n);

  /** Edge after {@code e} among the edges of {@code n}, or NULL. */
  Pointer agnxtedge(Pointer g, Pointer e, Pointer n);

  /** Graph that owns {@code obj}. */
  Pointer agraphof(Pointer obj);

  /** Root graph of {@code obj}. */
  Pointer agroot(Pointer obj);

  /** Non-zero when {@code g} contains {@code obj}. */
  int agcontains(Pointer g, Pointer obj);

  /** Name of a graph, node, edge, or the empty string. */
  String agnameof(Pointer obj);

  /**
   * Rename a node. Returns 0 on success.
   * libcgraph 2.42.4 segfaults inside this function, so callers on that version should not use it.
   */
  int agrelabel_node(Pointer node, String newName);

  /** Delete a node, edge, or subgraph from {@code g}. Returns 0 on success. */
  int agdelete(Pointer g, Pointer obj);

  /**
   * Delete a subgraph.
   * On Graphviz 2.42 the return value is the subgraph pointer, not 0.
   */
  long agdelsubg(Pointer g, Pointer sub);

  /** Delete a node. Returns 0 on success. */
  int agdelnode(Pointer g, Pointer node);

  /** Delete an edge. Returns 0 on success. */
  int agdeledge(Pointer g, Pointer edge);

  /** {@link AgobjKind} of {@code obj}. */
  int agobjkind(Pointer obj);

  /** Intern a copy of {@code text} in the graph's string dictionary. */
  Pointer agstrdup(Pointer g, String text);

  /** Intern {@code html} as an HTML string. */
  Pointer agstrdup_html(Pointer g, String html);

  /** Non-zero when {@code text} is an HTML string created by {@link #agstrdup_html}. */
  int aghtmlstr(Pointer text);

  /** Look up an interned string. Returns NULL when it is not present. */
  Pointer agstrbind(Pointer g, String text);

  /** Release an interned string. Returns 0 on success. */
  int agstrfree(Pointer g, Pointer text);

  /**
   * Canonical form of {@code text}. {@code flag} non-zero requests the HTML form.
   * The result is only valid until the next call.
   */
  String agcanon(String text, int flag);

  /** {@link #agcanon(String, int)} with a caller-owned C string. */
  String agcanon(Pointer text, int flag);

  /**
   * Write the canonical form of {@code text} into {@code buffer} and return that buffer.
   * {@code agstrcanon} calls {@code aghtmlstr}, which reads the refstr header in front of the pointer.
   * A temporary Java string is not a refstr, so pass storage from {@link #agstrdup} or a buffer whose
   * leading bytes are zero.
   */
  Pointer agstrcanon(String text, Pointer buffer);

  /** {@link #agstrcanon(String, Pointer)} with a caller-owned C string. */
  Pointer agstrcanon(Pointer text, Pointer buffer);

  /**
   * Canonical form of {@code text}, using cgraph's own buffer.
   * Same refstr requirement as {@link #agstrcanon(String, Pointer)}.
   */
  String agcanonStr(String text);

  /** {@link #agcanonStr(String)} with a caller-owned C string. */
  String agcanonStr(Pointer text);

  /**
   * Declare an attribute.
   * @param kind {@link AgobjKind#GRAPH}, {@link AgobjKind#NODE}, or {@link AgobjKind#EDGE}
   * @return the attribute symbol
   */
  Pointer agattr(Pointer g, int kind, String name, String defaultValue);

  /** Attribute symbol of {@code name} on {@code obj}, or NULL. */
  Pointer agattrsym(Pointer obj, String name);

  /** Next attribute of {@code kind} after {@code attr}, or the first when {@code attr} is NULL. */
  Pointer agnxtattr(Pointer g, int kind, Pointer attr);

  /** Copy string attributes from {@code from} to {@code to}. Returns 0 on success. */
  int agcopyattr(Pointer from, Pointer to);

  /**
   * Attach a record of {@code size} bytes, including the {@code Agrec_t} header, to {@code obj}.
   * @return the record
   */
  Pointer agbindrec(Pointer obj, String name, int size, int moveToFront);

  /** Find a record by name, or NULL. */
  Pointer aggetrec(Pointer obj, String name, int moveToFront);

  /** Delete a record. Returns 0 on success. */
  int agdelrec(Pointer obj, String name);

  /** Attach a record of {@code recordSize} bytes to every object of {@code kind}. */
  void aginit(Pointer g, int kind, String recordName, int recordSize, int moveToFront);

  /** Remove records named {@code recordName} from every object of {@code kind}. */
  void agclean(Pointer g, int kind, String recordName);

  /** Attribute value of {@code name} on {@code obj}, or NULL. */
  String agget(Pointer obj, String name);

  /** Value of attribute {@code sym} on {@code obj}. */
  String agxget(Pointer obj, Pointer sym);

  /** Set an attribute. Returns 0 on success. */
  int agset(Pointer obj, String name, String value);

  /** Set attribute {@code sym}. Returns 0 on success. */
  int agxset(Pointer obj, Pointer sym, String value);

  /** Set an attribute, declaring it with {@code defaultValue} when needed. Returns 0 on success. */
  int agsafeset(Pointer obj, String name, String value, String defaultValue);

  /** Find or create a subgraph. */
  Pointer agsubg(Pointer g, String name, int createflag);

  /** Find or create a subgraph by id. */
  Pointer agidsubg(Pointer g, @u_int64_t long id, int createflag);

  /** First subgraph, or NULL. */
  Pointer agfstsubg(Pointer g);

  /** Subgraph after {@code subg}, or NULL. */
  Pointer agnxtsubg(Pointer subg);

  /** Parent graph, or NULL for a root graph. */
  Pointer agparent(Pointer g);

  /** Number of nodes. */
  int agnnodes(Pointer g);

  /** Number of edges. */
  int agnedges(Pointer g);

  /** Number of subgraphs. */
  int agnsubg(Pointer g);

  /**
   * Degree of {@code n}.
   * @param in non-zero to count in-edges
   * @param out non-zero to count out-edges
   */
  int agdegree(Pointer g, Pointer n, int in, int out);

  /** Number of unique edges of {@code n} in the requested directions. */
  int agcountuniqedges(Pointer g, Pointer n, int in, int out);

  /** Allocate {@code size} bytes from the graph heap. */
  Pointer agalloc(Pointer g, @size_t long size);

  /** Resize a block from {@link #agalloc}. */
  Pointer agrealloc(Pointer g, Pointer ptr, @size_t long oldSize, @size_t long size);

  /** Free a block from {@link #agalloc}. */
  void agfree(Pointer g, Pointer ptr);

  /** Graph heap, or NULL when the default allocator has none. */
  Pointer agheap(Pointer g);

  /** Drop local name mappings. Used by the cgraph scanner. */
  void aginternalmapclearlocalnames(Pointer g);

  /** Set the error reporting level. Returns the previous level. */
  int agseterr(int level);

  /** Text of the last error, or NULL. */
  String aglasterr();

  /** Report an error with a printf format. Returns 0. */
  @Variadic(fixedCount = 2)
  int agerr(int level, String fmt);

  /** Report an error with one string argument. */
  @Variadic(fixedCount = 2)
  int agerr(int level, String fmt, String arg);

  /** Report an error with a string argument and an int argument. */
  @Variadic(fixedCount = 2)
  int agerr(int level, String fmt, String arg, int number);

  /** Report a formatted error at level {@link Agerrlevel#ERR}. */
  @Variadic(fixedCount = 1)
  void agerrorf(String fmt, String arg);

  /** Report a formatted warning. */
  @Variadic(fixedCount = 1)
  void agwarningf(String fmt, String arg);

  /** Number of errors reported since the last reset. */
  int agerrors();

  /** Set the error count to zero and return the previous count. */
  int agreseterrors();

  /** Install an error callback. Returns the previous callback. */
  Pointer agseterrf(AgCallbacks.ErrorFn handler);

  /** Restore an error callback previously returned by {@link #agseterrf(AgCallbacks.ErrorFn)}. */
  Pointer agseterrf(Pointer handler);

  /** Build or tear down the flat node and edge lists. Non-zero {@code flag} flattens. */
  void agflatten(Pointer g, int flag);

  /** Head node of an edge. */
  Pointer aghead(Pointer edge);

  /** Tail node of an edge. */
  Pointer agtail(Pointer edge);

  /** The opposite half of an edge pair. */
  Pointer agopp(Pointer edge);

  /** Non-zero when {@code a} and {@code b} are the same edge. */
  int ageqedge(Pointer a, Pointer b);
}
