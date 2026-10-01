package io.github.igrqb.jnr.graphviz;

import jnr.ffi.LibraryLoader;
import jnr.ffi.Memory;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loaded Graphviz libraries and the helpers the C API needs from Java:
 * argument vectors, in-memory streams, and render buffers.
 * <p>
 * libcgraph and libgvc are not safe to call from more than one thread.
 * Keep every {@link Pointer} returned here reachable until the native call that uses it has returned.
 * jnr frees a direct buffer when its pointer is collected.
 */
public final class NativeGraphviz {
  public static final LibC libc = LibraryLoader.create(LibC.class).load("c");
  public static final LibCgraph cgraph = LibraryLoader.create(LibCgraph.class).load("cgraph");
  public static final LibGvc gvc = LibraryLoader.create(LibGvc.class).load("gvc");
  public static final LibPack pack = LibraryLoader.create(LibPack.class).load("gvc");
  public static final Runtime runtime = Runtime.getRuntime(cgraph);

  private NativeGraphviz() {
  }

  /** {@code FILE*} stored in a libc stdio global such as {@link LibC#stdout()}. */
  public static Pointer stdio(jnr.ffi.Variable<Long> variable) {
    return Pointer.wrap(runtime, variable.get());
  }

  /** C string at {@code ptr}, or null when {@code ptr} is NULL. */
  public static String readString(Pointer ptr) {
    if (ptr == null || ptr.address() == 0) {
      return null;
    }
    return ptr.getString(0);
  }

  /**
   * NUL-terminated UTF-8 bytes that stay valid while {@code holder} is reachable.
   * Use this for {@link LibCgraph#agsetfile(Pointer)}.
   */
  public static Pointer stableCString(String text) {
    byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
    Pointer ptr = Memory.allocateDirect(runtime, bytes.length + 1L);
    ptr.put(0, bytes, 0, bytes.length);
    ptr.putByte(bytes.length, (byte) 0);
    return ptr;
  }

  /** {@code char **} for {@link LibGvc#gvParseArgs}. The result must stay reachable during the call. */
  public static Args args(String... values) {
    Pointer[] storage = new Pointer[values.length];
    Pointer argv = Memory.allocateDirect(runtime, (values.length + 1L) * Long.BYTES);
    for (int i = 0; i < values.length; i++) {
      storage[i] = stableCString(values[i]);
      argv.putPointer(i * 8L, storage[i]);
    }
    argv.putLong(values.length * 8L, 0);
    return new Args(values.length, argv, storage);
  }

  /** Argument vector allocated for {@link LibGvc#gvParseArgs}. */
  public static final class Args {
    public final int argc;
    public final Pointer argv;
    private final Pointer[] storage;

    private Args(int argc, Pointer argv, Pointer[] storage) {
      this.argc = argc;
      this.argv = argv;
      this.storage = storage;
    }
  }

  /**
   * Render {@code graph} and return the bytes. The buffer is released with {@link LibGvc#gvFreeRenderData}.
   * @return the render result. The length is the value libgvc wrote.
   */
  public static byte[] renderData(Pointer context, Pointer graph, String format) {
    Pointer result = Memory.allocateDirect(runtime, Long.BYTES);
    Pointer length = Memory.allocateDirect(runtime, Long.BYTES);
    result.putLong(0, 0);
    length.putLong(0, 0);
    int status = gvc.gvRenderData(context, graph, format, result, length);
    if (status != 0) {
      throw new IllegalStateException("gvRenderData returned " + status + " " + String.valueOf(cgraph.aglasterr()));
    }
    int size = length.getInt(0);
    Pointer data = result.getPointer(0);
    byte[] bytes = new byte[size];
    if (size > 0) {
      data.get(0, bytes, 0, size);
    }
    gvc.gvFreeRenderData(data);
    return bytes;
  }

  /**
   * Package name, version, and build date from {@link LibGvc#gvcInfo}.
   * The array is three strings.
   */
  public static String[] info(Pointer context) {
    Pointer list = gvc.gvcInfo(context);
    String[] values = new String[3];
    for (int i = 0; i < values.length; i++) {
      values[i] = readString(list.getPointer(i * 8L));
    }
    return values;
  }

  /**
   * Plugin names of {@code kind}. Frees the list libgvc allocated.
   */
  public static List<String> plugins(Pointer context, String kind) {
    Pointer count = Memory.allocateDirect(runtime, Integer.BYTES);
    count.putInt(0, 0);
    Pointer list = gvc.gvPluginList(context, kind, count, "");
    int size = count.getInt(0);
    List<String> names = new ArrayList<>();
    if (list != null && list.address() != 0) {
      for (int i = 0; i < size; i++) {
        Pointer item = list.getPointer(i * 8L);
        names.add(readString(item));
        libc.free(item);
      }
      libc.free(list);
    }
    return names;
  }

  /**
   * Run {@code action} while C stdout is redirected to a buffer.
   * Restores file descriptor 1 before returning.
   */
  public static byte[] captureStdout(Runnable action) throws IOException {
    Path file = Files.createTempFile("jnr-graphviz-", ".out");
    Pointer stream = libc.fopen(file.toString(), "w");
    if (stream == null || stream.address() == 0) {
      throw new IllegalStateException("fopen " + file);
    }
    System.out.flush();
    System.err.flush();
    libc.fflush(stdio(libc.stdout()));
    int saved = libc.dup(1);
    try {
      if (libc.dup2(libc.fileno(stream), 1) < 0) {
        throw new IllegalStateException("dup2");
      }
      action.run();
      libc.fflush(stdio(libc.stdout()));
    } finally {
      if (saved >= 0) {
        libc.dup2(saved, 1);
        libc.close(saved);
      }
      libc.fclose(stream);
    }
    byte[] bytes = Files.readAllBytes(file);
    Files.deleteIfExists(file);
    return bytes;
  }

  /** FILE backed by a growable buffer, for {@code agwrite} and {@code gvRender}. */
  public static final class MemStream implements AutoCloseable {
    private final Pointer dataPtr = Memory.allocateDirect(runtime, Long.BYTES);
    private final Pointer sizePtr = Memory.allocateDirect(runtime, Long.BYTES);
    private final Pointer file;
    private boolean closed;

    public MemStream() {
      dataPtr.putLong(0, 0);
      sizePtr.putLong(0, 0);
      file = libc.open_memstream(dataPtr, sizePtr);
      if (file == null || file.address() == 0) {
        throw new IllegalStateException("open_memstream");
      }
    }

    public Pointer file() {
      return file;
    }

    /** Flush the stream and return the bytes written so far. */
    public byte[] toByteArray() {
      libc.fflush(file);
      long size = sizePtr.getLong(0);
      if (size > Integer.MAX_VALUE) {
        throw new IllegalStateException("stream is larger than an array");
      }
      int n = (int) size;
      byte[] bytes = new byte[n];
      Pointer data = dataPtr.getPointer(0);
      if (n > 0 && data != null && data.address() != 0) {
        data.get(0, bytes, 0, n);
      }
      return bytes;
    }

    @Override
    public void close() {
      if (closed) {
        return;
      }
      closed = true;
      Pointer data = dataPtr.getPointer(0);
      libc.fclose(file);
      if (data != null && data.address() != 0) {
        libc.free(data);
      }
    }
  }
}
