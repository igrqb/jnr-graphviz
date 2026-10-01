/* dot.demo/dot.c from https://gitlab.com/graphviz/graphviz/-/blob/main/dot.demo/dot.c
 * gvNextInputGraph, gvLayoutJobs, gvRenderJobs.
 */
#include <graphviz/gvc.h>
#include <stddef.h>

int main(int argc, char **argv) {
  GVC_t *gvc = gvContext();
  gvParseArgs(gvc, argc, argv);

  graph_t *g, *prev = NULL;
  while ((g = gvNextInputGraph(gvc))) {
    if (prev) {
      gvFreeLayout(gvc, prev);
      agclose(prev);
    }
    gvLayoutJobs(gvc, g);
    gvRenderJobs(gvc, g);
    prev = g;
  }
  return gvFreeContext(gvc);
}
