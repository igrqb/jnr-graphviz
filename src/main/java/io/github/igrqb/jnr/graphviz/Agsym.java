package io.github.igrqb.jnr.graphviz;

import jnr.ffi.Pointer;

/**
 * Readers for {@code Agsym_t} on LP64. The symbol starts with a 16-byte {@code Dtlink_t},
 * then {@code char *name} and {@code char *defval}.
 */
public final class Agsym {
  public static final int NAME_OFFSET = 16;
  public static final int DEFVAL_OFFSET = 24;

  private Agsym() {
  }

  /** Attribute name, or null. */
  public static String name(Pointer sym) {
    return text(sym, NAME_OFFSET);
  }

  /** Default value, or null. */
  public static String defval(Pointer sym) {
    return text(sym, DEFVAL_OFFSET);
  }

  private static String text(Pointer sym, int offset) {
    if (sym == null || sym.address() == 0) {
      return null;
    }
    Pointer value = sym.getPointer(offset);
    if (value == null || value.address() == 0) {
      return null;
    }
    return value.getString(0);
  }
}
