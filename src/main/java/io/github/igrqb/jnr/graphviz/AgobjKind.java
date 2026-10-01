package io.github.igrqb.jnr.graphviz;

/**
 * Object tags from {@code cgraph.h}. {@link AgobjKind#EDGE} is an out-edge.
 */
public final class AgobjKind {
  public static final int GRAPH = 0;
  public static final int NODE = 1;
  public static final int OUTEDGE = 2;
  public static final int EDGE = OUTEDGE;
  public static final int INEDGE = 3;

  private AgobjKind() {
  }
}
