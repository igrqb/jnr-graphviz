package io.github.igrqb.jnr.graphviz;

/** Error levels passed to {@link LibCgraph#agseterr} and {@link LibCgraph#agerr}. */
public final class Agerrlevel {
  public static final int WARN = 0;
  public static final int ERR = 1;
  public static final int MAX = 2;
  public static final int PREV = 3;

  private Agerrlevel() {
  }
}
