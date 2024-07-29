package io.github.igrqb.jnr.graphviz;

import jnr.ffi.LibraryLoader;
import jnr.ffi.Memory;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * User-friendly API to leverage io.github.igrqb.jnr.graphviz.Graphviz library functions to generate graph output from dot language.
 */
public class Graphviz {
  private static final Logger log = LoggerFactory.getLogger(Graphviz.class);

  private static final LibGvc libGvc;
  private static final LibC libc;

  static {
    libGvc = LibraryLoader.create(LibGvc.class).load("gvc");
    libc = LibraryLoader.create(LibC.class).load("c");
  }

  /**
   * Generates SVG drawing of input graph specified in dot language.
   * This method is the JNR equivalent of the following C implementation:
   * <pre>
   * {@code
   * #include <graphviz/gvc.h>
   * #include <stddef.h>
   * #include <stdio.h>
   * #include <stdlib.h>
   *
   * int main(int argc, char **argv) {
   *
   *   char* buffer;
   *   size_t bufferSize = 0;
   *   FILE* myStream = open_memstream(&buffer, &bufferSize);
   *
   *   GVC_t *gvc = gvContext();
   *
   *   graph_t *g = agmemread("digraph { a -> b; b -> c }");
   *   gvLayout(gvc, g, "dot");
   *   gvRender(gvc, g, "svg", myStream);
   *
   *   fclose(myStream);
   *
   *   printf("SVG:\n----------\nsize=%d\n----------\n%s\n", bufferSize, buffer);
   *
   *   free(buffer);
   *
   *   gvFreeLayout(gvc, g);
   *   agclose(g);
   *
   *   return gvFreeContext(gvc);
   * }
   * }
   * </pre>
   * @param dot input dot language representation of graph
   * @return SVG of graph
   */
  public static String dotToSvg(String dot) {
    Pointer bufferAddress = Memory.allocateDirect(Runtime.getRuntime(libc), Long.BYTES);
    Pointer bufferSize = Memory.allocateDirect(Runtime.getRuntime(libc), Integer.BYTES);
    Pointer memStreamPtr = libc.open_memstream(bufferAddress, bufferSize);

    Pointer gvc = libGvc.gvContext();
    Pointer g = libGvc.agmemread(dot);
    int status = libGvc.gvLayout(gvc, g, "dot");
    log.trace("gvLayout status = {}", status);

    status = libGvc.gvRender(gvc, g, "svg", memStreamPtr);
    log.trace("gvRender status = {}", status);
    status = libc.fflush(memStreamPtr);
    log.trace("fflush status = {}", status);

    int size = bufferSize.getInt(0);
    Pointer buffer = Pointer.wrap(Runtime.getRuntime(libc), bufferAddress.getLong(0));
    String s = buffer.getString(0);
    log.trace("String length = {}", size);
    log.trace("String =\n{}", s);

    status = libGvc.gvFreeLayout(gvc, g);
    log.trace("gvFreeLayout status = {}", status);
    status = libGvc.agclose(g);
    log.trace("agclose status = {}", status);
    status = libGvc.gvFreeContext(gvc);
    log.trace("gvFreeContext status = {}", status);
    status = libc.fclose(memStreamPtr);
    log.trace("fclose status = {}", status);
    libc.free(buffer);
    return s;
  }

  /**
   * Generate SVG file from dot language representation of graph.
   * @param dot input dot spec of graph
   * @param svgFilePath desired output SVG file path
   * @return output SVG file
   */
  public static File dotToSvg(String dot, String svgFilePath) {
    Pointer fp = libc.fopen(svgFilePath, "w");

    Pointer gvc = libGvc.gvContext();
    Pointer g = libGvc.agmemread(dot);
    int status = libGvc.gvLayout(gvc, g, "dot");
    log.trace("gvLayout status = {}", status);

    status = libGvc.gvRender(gvc, g, "svg", fp);
    log.trace("gvRender status = {}", status);
    status = libc.fflush(fp);
    log.trace("fflush status = {}", status);

    status = libGvc.gvFreeLayout(gvc, g);
    log.trace("gvFreeLayout status = {}", status);
    status = libGvc.agclose(g);
    log.trace("agclose status = {}", status);
    status = libGvc.gvFreeContext(gvc);
    log.trace("gvFreeContext status = {}", status);
    status = libc.fclose(fp);
    log.trace("fclose status = {}", status);

    return new File(svgFilePath);
  }

  /**
   * Export graph in dot format to specified output format.<br/>
   * See <a href="https://graphviz.org/docs/outputs/">Output Formats | Graphviz</a> for available formats.
   *
   * @param dot graph in dot format
   * @param outputFormat target output format
   * @return graph exported to target output format
   */
  public static byte[] export(String dot, OutputFormat outputFormat) {
    Pointer bufferAddress = Memory.allocateDirect(Runtime.getRuntime(libc), Long.BYTES);
    Pointer bufferSize = Memory.allocateDirect(Runtime.getRuntime(libc), Integer.BYTES);
    Pointer memStreamPtr = libc.open_memstream(bufferAddress, bufferSize);

    Pointer gvc = libGvc.gvContext();
    Pointer g = libGvc.agmemread(dot);
    int status = libGvc.gvLayout(gvc, g, "dot");
    log.trace("gvLayout status = {}", status);

    status = libGvc.gvRender(gvc, g, outputFormat.getCommandLineParameter(), memStreamPtr);
    log.trace("gvRender status = {}", status);
    status = libc.fflush(memStreamPtr);
    log.trace("fflush status = {}", status);

    int size = bufferSize.getInt(0);
    Pointer buffer = Pointer.wrap(Runtime.getRuntime(libc), bufferAddress.getLong(0));
    byte[] bytes = new byte[size];
    buffer.get(0, bytes, 0, size);
    log.trace("Output buffer length = {}", size);

    status = libGvc.gvFreeLayout(gvc, g);
    log.trace("gvFreeLayout status = {}", status);
    status = libGvc.agclose(g);
    log.trace("agclose status = {}", status);
    status = libGvc.gvFreeContext(gvc);
    log.trace("gvFreeContext status = {}", status);
    status = libc.fclose(memStreamPtr);
    log.trace("fclose status = {}", status);
    libc.free(buffer);
    return bytes;
  }
}
