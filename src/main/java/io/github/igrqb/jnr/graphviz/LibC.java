package io.github.igrqb.jnr.graphviz;

import jnr.ffi.Pointer;
import jnr.ffi.Variable;
import jnr.ffi.types.int64_t;

/**
 * JNR interface to libc that exposes a subset of required functions to work with I/O streams.
 */
public interface LibC {

  @int64_t
  Variable<Long> stderr();
  @int64_t Variable<Long> stdout();
  @int64_t Variable<Long> stdin();

  Pointer open_memstream(Pointer stringPtr, Pointer sizePtr);

  /**
   * Open a file for read/write.
   * See <a href="https://en.cppreference.com/w/cpp/io/c/fopen">std::fopen</a>
   * @param filename file name to associate the file stream to
   * @param mode null-terminated character string determining file access mode
   * @return file stream
   */
  Pointer fopen(String filename, String mode);

  int fclose(Pointer stream);
  int fflush(Pointer stream);
  void free(Pointer ptr);
}
