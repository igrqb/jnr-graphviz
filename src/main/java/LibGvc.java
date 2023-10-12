import jnr.ffi.Pointer;

/**
 * JNR interface to libgvc - Graphviz context library. Exposes a subset of function required for conversion from dot to
 * svg.
 */
public interface LibGvc {
  Pointer gvContext();
  Pointer agmemread(String dotText);
  int gvLayout(Pointer gvc, Pointer g, String engine);
  int gvRender(Pointer gvc, Pointer g, String format, Pointer out);
  int gvFreeLayout(Pointer gvc, Pointer g);
  int agclose(Pointer g);
  int gvFreeContext(Pointer gvc);
}
