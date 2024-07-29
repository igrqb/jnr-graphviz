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

  /**
   * Open an in-memory stream
   * @param dataPtr pointer where the data will reside
   * @param sizePtr pointer where information on the size of the data will reside
   * @return pointer to the memory stream
   * @see <a href="https://www.man7.org/linux/man-pages/man3/open_memstream.3.html">open_memstream(3) - Linux manual page</a>
   */
  Pointer open_memstream(Pointer dataPtr, Pointer sizePtr);

  /**
   * Open a file for read/write.
   * See <a href="https://en.cppreference.com/w/cpp/io/c/fopen">std::fopen</a>
   * @param filename file name to associate the file stream to
   * @param mode null-terminated character string determining file access mode
   * @return file stream
   */
  Pointer fopen(String filename, String mode);

  /**
   * Close file stream
   * @param stream pointer to file stream
   * @return return code of operation
   */
  int fclose(Pointer stream);

  /**
   * Flush a file stream
   * @param stream pointer to file stream
   * @return return code of operation
   */
  int fflush(Pointer stream);

  /**
   * Free a pointer
   * @param ptr pointer
   */
  void free(Pointer ptr);
}
