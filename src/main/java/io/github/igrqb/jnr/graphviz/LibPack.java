package io.github.igrqb.jnr.graphviz;

import jnr.ffi.Pointer;

/**
 * JNR bindings for the graph-packing functions declared in {@code pack.h}.
 * They are exported by libgvc.
 * <p>
 * {@code pack_info}, {@code boxf}, and {@code point} are caller-allocated structs.
 * Pass a pointer to memory of the C size, or NULL where the C signature allows it.
 * Counts are {@code int *} written by {@code ccomps}, {@code cccomps}, and {@code pccomps}.
 * The returned graph arrays are malloc'd and are freed with {@code free}.
 * <p>
 * {@code mapClust} requires the layout operation state. Calling it on its own aborts.
 */
public interface LibPack {

  /** Connected-component subgraphs. {@code count} receives the length. {@code namePrefix} may be NULL. */
  Pointer ccomps(Pointer g, Pointer count, String namePrefix);

  /** Cluster-aware connected components. */
  Pointer cccomps(Pointer g, Pointer count, String namePrefix);

  /**
   * Packing components.
   * @param changed {@code boolean *}. libgvc 2.42 writes through this pointer, so it must not be NULL.
   */
  Pointer pccomps(Pointer g, Pointer count, String namePrefix, Pointer changed);

  /** Induce edges of the root graph into a component subgraph. Returns non-zero on success. */
  int nodeInduce(Pointer g);

  /**
   * Pack laid-out component graphs into {@code root}.
   * @param fixed {@code boolean *} per component, or NULL
   * @return 0 on success
   */
  int pack_graph(int count, Pointer graphs, Pointer root, Pointer fixed);

  /** Non-zero when {@code g} is connected. The graph needs the layout info records installed. */
  int isConnected(Pointer g);

  /** Pack mode stored on the graph, or {@code defaultMode}. */
  int getPackMode(Pointer g, int defaultMode);

  /** Pack margin stored on the graph, or {@code defaultValue} when unset. */
  int getPack(Pointer g, int unsetValue, int defaultValue);

  /** Fill {@code info} ({@code pack_info *}) from the graph. Returns the mode. */
  int getPackInfo(Pointer g, int defaultMode, int defaultMargin, Pointer info);

  /** Fill the mode fields of {@code info}. Returns the mode. */
  int getPackModeInfo(Pointer g, int defaultMode, Pointer info);

  /** Parse a pack-mode string such as {@code "array_3"} into {@code info}. Returns the mode. */
  int parsePackModeInfo(String text, int defaultMode, Pointer info);

  /** Pack graphs described by {@code info} into {@code root}. */
  int packGraphs(int count, Pointer graphs, Pointer root, Pointer info);

  /** Pack subgraphs described by {@code info} into {@code root}. */
  int packSubgraphs(int count, Pointer graphs, Pointer root, Pointer info);

  /** Place graphs and return the array of {@code point} offsets. */
  Pointer putGraphs(int count, Pointer graphs, Pointer root, Pointer info);

  /** Place rectangles and return the array of {@code point} offsets. */
  Pointer putRects(int count, Pointer boxes, Pointer info);

  /** Pack rectangles in place. {@code boxes} is a {@code boxf *}. Returns 0 on success. */
  int packRects(int count, Pointer boxes, Pointer info);

  /** Move laid-out graphs by the {@code point *} offsets in {@code shifts}. */
  int shiftGraphs(int count, Pointer graphs, Pointer shifts, Pointer root, int doSplines);

  /** Map cluster subgraphs to a graph. Requires an active layout operation. */
  Pointer mapClust(Pointer g);
}
