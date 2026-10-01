package io.github.igrqb.jnr.graphviz;

/** {@code PK_*} flags from {@code pack.h}, stored in {@code pack_info.flags}. */
public final class PackFlags {
  public static final int COL_MAJOR = 1;
  public static final int USER_VALS = 1 << 1;
  public static final int LEFT_ALIGN = 1 << 2;
  public static final int RIGHT_ALIGN = 1 << 3;
  public static final int TOP_ALIGN = 1 << 4;
  public static final int BOT_ALIGN = 1 << 5;
  public static final int INPUT_ORDER = 1 << 6;

  private PackFlags() {
  }
}
