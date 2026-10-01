package io.github.igrqb.jnr.graphviz;

import jnr.ffi.Pointer;
import jnr.ffi.Variable;
import jnr.ffi.types.size_t;

/**
 * JNR interface to libc that exposes a subset of required functions to work with I/O streams.
 */
public interface LibC {

  /**
   * Address of the {@code FILE} for stderr.
   * jnr 2.3 cannot generate a {@code Variable} of {@code Pointer}, so this is the raw address.
   * @return the stderr stream address
   */
  Variable<Long> stderr();

  /**
   * Address of the {@code FILE} for stdout.
   * @return the stdout stream address
   */
  Variable<Long> stdout();

  /**
   * Address of the {@code FILE} for stdin.
   * @return the stdin stream address
   */
  Variable<Long> stdin();

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

  /**
   * Open a memory buffer as a FILE.
   * @param buf buffer
   * @param size number of bytes in the buffer
   * @param mode file access mode, for example {@code "r"}
   * @return the stream, or NULL
   * @see <a href="https://www.man7.org/linux/man-pages/man3/fmemopen.3.html">fmemopen(3)</a>
   */
  Pointer fmemopen(Pointer buf, @size_t long size, String mode);

  /**
   * File descriptor of a stream.
   * @param stream FILE*
   * @return the descriptor, or -1
   */
  int fileno(Pointer stream);

  /**
   * Duplicate a file descriptor.
   * @param fd descriptor
   * @return the new descriptor, or -1
   */
  int dup(int fd);

  /**
   * Make {@code newfd} a duplicate of {@code oldfd}.
   * @param oldfd source descriptor
   * @param newfd target descriptor
   * @return {@code newfd}, or -1
   */
  int dup2(int oldfd, int newfd);

  /**
   * Close a file descriptor.
   * @param fd descriptor
   * @return 0 on success, -1 on error
   */
  int close(int fd);
}
