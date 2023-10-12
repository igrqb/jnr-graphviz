import jnr.ffi.Pointer;
import jnr.ffi.Variable;
import jnr.ffi.types.int64_t;

/**
 * JNR interface to libc that exposes a subset of required functions to work with I/O streams.
 */
public interface LibC {
  Pointer fopen(String s, String mode);
  @int64_t
  Variable<Long> stderr();
  @int64_t Variable<Long> stdout();
  @int64_t Variable<Long> stdin();

  Pointer open_memstream(Pointer stringPtr, Pointer sizePtr);
  int fclose(Pointer stream);
  int fflush(Pointer stream);
  void free(Pointer ptr);
}
