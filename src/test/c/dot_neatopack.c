/* dot.demo/neatopack.c from https://gitlab.com/graphviz/graphviz/-/blob/main/dot.demo/neatopack.c
 * aginit, ccomps, nodeInduce, gvLayout, pack_graph, gvRender, agdelete.
 *
 * Graphviz 2.42 exports nodeInduce and passes the component count as int.
 * Current upstream calls graphviz_node_induce and uses size_t. This file follows
 * the headers installed with the library under test. The render format is the
 * upstream "ps".
 */
#include <graphviz/cgraph.h>
#include <graphviz/gvc.h>
#include <graphviz/pack.h>
#include <stdio.h>
#include <stdlib.h>

int main(int argc, char **argv) {
  GVC_t *gvc = gvContext();

  FILE *fp;
  if (argc > 1)
    fp = fopen(argv[1], "r");
  else
    fp = stdin;
  graph_t *g = agread(fp, NULL);

  aginit(g, AGRAPH, "Agraphinfo_t", sizeof(Agraphinfo_t), 1);
  aginit(g, AGNODE, "Agnodeinfo_t", sizeof(Agnodeinfo_t), 1);

  int ncc = 0;
  graph_t **cc = ccomps(g, &ncc, NULL);

  for (int i = 0; i < ncc; i++) {
    graph_t *sg = cc[i];
    nodeInduce(sg);
    gvLayout(gvc, sg, "neato");
  }
  pack_graph(ncc, cc, g, 0);

  gvRender(gvc, g, "ps", stdout);
  fflush(stdout);

  /* gvFreeLayout on a packed component aborts in Graphviz 2.42.4
   * (munmap_chunk invalid pointer) after the PostScript is written.
   * agdelete releases the component. The bytes above stay the same. */
  for (int i = 0; i < ncc; i++) {
    agdelete(g, cc[i]);
  }
  free(cc);
  agclose(g);
  return gvFreeContext(gvc);
}
