package io.github.igrqb.jnr.graphviz;

import jnr.ffi.Memory;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.ffi.annotations.Delegate;

/**
 * Callbacks and the {@code Agcbdisc_t} struct from {@code cgraph.h}.
 * The nine function pointers are {@code graph}, {@code node}, and {@code edge},
 * each with {@code ins}, {@code mod}, and {@code del}, in that order.
 */
public final class AgCallbacks {
  private AgCallbacks() {
  }

  /** {@code agobjfn_t}. */
  public interface ObjectFn {
    @Delegate
    void call(Pointer graph, Pointer object, Pointer state);
  }

  /** {@code agobjupdfn_t}. */
  public interface UpdateFn {
    @Delegate
    void call(Pointer graph, Pointer object, Pointer state, Pointer sym);
  }

  /** {@code agusererrf}. The message is one piece of an error report. */
  public interface ErrorFn {
    @Delegate
    int call(String message);
  }

  /** {@code Agcbdisc_t}. Unused slots stay NULL. */
  public static final class Disc extends Struct {
    public final Function<ObjectFn> graphIns = function(ObjectFn.class);
    public final Function<UpdateFn> graphMod = function(UpdateFn.class);
    public final Function<ObjectFn> graphDel = function(ObjectFn.class);
    public final Function<ObjectFn> nodeIns = function(ObjectFn.class);
    public final Function<UpdateFn> nodeMod = function(UpdateFn.class);
    public final Function<ObjectFn> nodeDel = function(ObjectFn.class);
    public final Function<ObjectFn> edgeIns = function(ObjectFn.class);
    public final Function<UpdateFn> edgeMod = function(UpdateFn.class);
    public final Function<ObjectFn> edgeDel = function(ObjectFn.class);
    private jnr.ffi.Pointer stable;

    public Disc(Runtime runtime) {
      super(runtime);
      jnr.ffi.Pointer memory = Struct.getMemory(this);
      memory.setMemory(0, Struct.size(this), (byte) 0);
    }

    /**
     * Native address of this {@code Agcbdisc_t}.
     * The struct is copied into direct memory. jnr's struct buffer is a Java array,
     * and a native call only sees a temporary copy of that array.
     * {@code agpushdisc} keeps the pointer, so the copy has to outlive the call.
     */
    public jnr.ffi.Pointer pointer() {
      int size = Struct.size(this);
      if (stable == null) {
        stable = Memory.allocateDirect(getRuntime(), size);
      }
      byte[] bytes = new byte[size];
      Struct.getMemory(this).get(0, bytes, 0, size);
      stable.put(0, bytes, 0, size);
      return stable;
    }
  }
}
