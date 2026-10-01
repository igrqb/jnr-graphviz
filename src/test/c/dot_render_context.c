/* gvRenderContext from gvc.h. A NULL context renders to stdout. */
#include <graphviz/gvc.h>
#include <stdlib.h>

int main(void) {
  GVC_t *gvc = gvContext();
  graph_t *g = agmemread("digraph { a -> b }");
  gvLayout(gvc, g, "dot");
  gvRenderContext(gvc, g, "svg", NULL);
  gvFreeLayout(gvc, g);
  agclose(g);
  gvFinalize(gvc);
  return gvFreeContext(gvc);
}
