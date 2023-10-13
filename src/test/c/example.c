#include <graphviz/gvc.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>

/*
    Example program that generates an SVG representation of a dot language graph using the Graphviz C libraries.
    The Graphviz renderer outputs to an output stream so, to capture as a string, we need to create a memstream
    using open_memstream.

    This example is based on: https://gitlab.com/graphviz/graphviz/-/blob/main/dot.demo/simple.c
*/

int main(int argc, char **argv) {

  // Set up the memory stream
  char* buffer;
  size_t bufferSize = 0;
  FILE* myStream = open_memstream(&buffer, &bufferSize);

  // Set up a graphviz context
  GVC_t *gvc = gvContext();

  // Create a graph from an in-memory definition in dot language
  graph_t *g = agmemread("digraph { a -> b; b -> c }");
  // Set the layout as dot
  gvLayout(gvc, g, "dot");
  // Render the graph in svg format to the memory stream
  gvRender(gvc, g, "svg", myStream);

  // (Flush and) close the memory stream. This sets the bufferSize with the length of the SVG string
  // and the buffer with the SVG string contents.
  fclose(myStream);

  // Print out the size of the SVG string and its contents
  printf("SVG:\n----------\nsize=%d\n----------\n%s\n", bufferSize, buffer);

  // Free memory and Graphviz resources
  free(buffer);

  gvFreeLayout(gvc, g);
  agclose(g);

  return gvFreeContext(gvc);
}
