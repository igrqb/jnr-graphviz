/* dot.demo/example.c from https://gitlab.com/graphviz/graphviz/-/blob/main/dot.demo/example.c
 * agopen, agnode, agedge, agsafeset, agwrite.
 */
#include <graphviz/gvc.h>
#include <stdio.h>
#include <stdlib.h>

#define NO_LAYOUT_OR_RENDERING

int main(void) {
#ifndef NO_LAYOUT_OR_RENDERING
  GVC_t *gvc = gvContext();
#endif

  Agraph_t *g = agopen("g", Agdirected, 0);
  Agnode_t *n = agnode(g, "n", 1);
  Agnode_t *m = agnode(g, "m", 1);
  (void)agedge(g, n, m, 0, 1);

  agsafeset(n, "color", "red", "");

#ifdef NO_LAYOUT_OR_RENDERING
  agwrite(g, stdout);
#else
  gvLayout(gvc, g, "dot");
  gvRender(gvc, g, "dot", stdout);
  gvFreeLayout(gvc, g);
#endif

  agclose(g);
  return EXIT_SUCCESS;
}
