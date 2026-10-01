package io.github.igrqb.jnr.graphviz;

/**
 * Bits of the {@code Agdesc_t} descriptor passed by value to {@link LibCgraph#agopen}.
 * <p>
 * The installed Graphviz library packs these bits into one 32-bit word, least significant bit first.
 * {@link #directed()} and the other readers return the descriptors exported by libcgraph
 * ({@code Agdirected}, {@code Agstrictdirected}, {@code Agundirected}, {@code Agstrictundirected}).
 */
public final class Agdesc {
  public static final int DIRECTED = 1;
  public static final int STRICT = 1 << 1;
  public static final int NO_LOOP = 1 << 2;
  public static final int MAINGRAPH = 1 << 3;
  public static final int FLATLOCK = 1 << 4;
  public static final int NO_WRITE = 1 << 5;
  public static final int HAS_ATTRS = 1 << 6;
  public static final int HAS_CMPND = 1 << 7;

  private Agdesc() {
  }

  /** Combine descriptor bits. */
  public static int of(int... bits) {
    int value = 0;
    for (int bit : bits) {
      value |= bit;
    }
    return value;
  }

  /** {@code Agdirected}. */
  public static int directed() {
    return NativeGraphviz.cgraph.Agdirected().get();
  }

  /** {@code Agstrictdirected}. */
  public static int strictDirected() {
    return NativeGraphviz.cgraph.Agstrictdirected().get();
  }

  /** {@code Agundirected}. */
  public static int undirected() {
    return NativeGraphviz.cgraph.Agundirected().get();
  }

  /** {@code Agstrictundirected}. */
  public static int strictUndirected() {
    return NativeGraphviz.cgraph.Agstrictundirected().get();
  }
}
