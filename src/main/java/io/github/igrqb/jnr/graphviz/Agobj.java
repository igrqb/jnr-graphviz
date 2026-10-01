package io.github.igrqb.jnr.graphviz;

import jnr.ffi.Pointer;

/**
 * Readers for the {@code Agobj_t} header. The header starts with {@code Agtag_t},
 * whose {@code id} is a {@code uint64_t} at offset 8 on LP64.
 */
public final class Agobj {
  public static final int ID_OFFSET = 8;

  private Agobj() {
  }

  /** {@code AGID(obj)}. */
  public static long id(Pointer obj) {
    return obj.getLong(ID_OFFSET);
  }
}
