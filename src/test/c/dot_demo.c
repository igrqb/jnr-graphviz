/* dot.demo/demo.c from https://gitlab.com/graphviz/graphviz/-/blob/main/dot.demo/demo.c
 * gvParseArgs, agopen, agnode, agedge, agsafeset, gvLayoutJobs, gvRenderJobs.
 * argv[0] or -K selects the layout engine. The test runs this with -Kdot -Tplain.
 */
#include <graphviz/gvc.h>

int main(int argc, char **argv) {
  GVC_t *gvc = gvContext();
  gvParseArgs(gvc, argc, argv);

  Agraph_t *g = agopen("g", Agdirected, 0);
  Agnode_t *n = agnode(g, "n", 1);
  Agnode_t *m = agnode(g, "m", 1);
  (void)agedge(g, n, m, 0, 1);
  agsafeset(n, "color", "red", "");

  gvLayoutJobs(gvc, g);
  gvRenderJobs(gvc, g);

  gvFreeLayout(gvc, g);
  agclose(g);
  return gvFreeContext(gvc);
}
