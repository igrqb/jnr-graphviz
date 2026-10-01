/* Calls the libcgraph, libgvc, and pack entry points and prints their results.
 * The Java test repeats the same calls and compares this transcript.
 */
#include <graphviz/cgraph.h>
#include <graphviz/gvc.h>
#include <graphviz/pack.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* Exported by libgvc 2.42 and declared in later gvc.h. */
extern GVC_t *gvCloneGVC(GVC_t *);
extern void gvFreeCloneGVC(GVC_t *);

static int inserts = 0;

static void on_ins(Agraph_t *g, Agobj_t *obj, void *arg) {
  (void)arg;
  inserts++;
  printf("ins %s %s\n", agnameof(g), agnameof(obj));
}

static void on_mod(Agraph_t *g, Agobj_t *obj, void *arg, Agsym_t *sym) {
  (void)g;
  (void)obj;
  (void)arg;
  (void)sym;
}

static void on_del(Agraph_t *g, Agobj_t *obj, void *arg) {
  (void)g;
  (void)obj;
  (void)arg;
}

static int errfn(char *msg) {
  printf("ERRFN:%s\n", msg == NULL ? "null" : msg);
  return 0;
}

static void print_str(const char *s) {
  if (s == NULL) printf("null");
  else printf("%s", s);
}

static unsigned desc_bits(Agdesc_t desc) {
  unsigned bits = 0;
  memcpy(&bits, &desc, sizeof bits);
  return bits;
}

static void dump_graph(const char *label, Agraph_t *g) {
  printf("%s name=%s directed=%d undirected=%d strict=%d simple=%d nodes=%d edges=%d subg=%d kind=%d root=%s\n",
    label, agnameof(g), agisdirected(g), agisundirected(g), agisstrict(g), agissimple(g),
    agnnodes(g), agnedges(g), agnsubg(g), agobjkind(g), agnameof(agroot(g)));
}

static void walk_nodes(const char *label, Agraph_t *g) {
  printf("%s", label);
  for (Agnode_t *n = agfstnode(g); n; n = agnxtnode(g, n)) printf(" %s", agnameof(n));
  printf("\n");
}

static char *slurp_write(Agraph_t *g) {
  char *buf = NULL;
  size_t n = 0;
  FILE *f = open_memstream(&buf, &n);
  int rc = agwrite(g, f);
  fclose(f);
  printf("agwrite %d bytes %zu\n", rc, n);
  fwrite(buf, 1, n, stdout);
  if (n == 0 || buf[n - 1] != '\n') printf("\n");
  free(buf);
  return NULL;
}

static void slurp_file(FILE *f) {
  char tmp[256];
  size_t n;
  while ((n = fread(tmp, 1, sizeof tmp, f)) > 0) fwrite(tmp, 1, n, stdout);
}

int main(void) {
  setvbuf(stdout, NULL, _IONBF, 0);
  printf("Agdirected %u\n", desc_bits(Agdirected));
  printf("Agstrictdirected %u\n", desc_bits(Agstrictdirected));
  printf("Agundirected %u\n", desc_bits(Agundirected));
  printf("Agstrictundirected %u\n", desc_bits(Agstrictundirected));

  Agraph_t *kinds[4];
  Agdesc_t descs[4] = {Agdirected, Agstrictdirected, Agundirected, Agstrictundirected};
  char *kind_names[4] = {"directed", "strictdirected", "undirected", "strictundirected"};
  for (int i = 0; i < 4; i++) {
    kinds[i] = agopen(kind_names[i], descs[i], NULL);
    dump_graph(kind_names[i], kinds[i]);
    printf("parent %s\n", agparent(kinds[i]) ? agnameof(agparent(kinds[i])) : "null");
    agclose(kinds[i]);
  }

  unsigned built = 0;
  built |= 1u;      /* directed */
  built |= 1u << 3; /* maingraph */
  Agdesc_t built_desc;
  memcpy(&built_desc, &built, sizeof built_desc);
  Agraph_t *built_g = agopen("built", built_desc, NULL);
  dump_graph("built", built_g);
  agclose(built_g);

  Agraph_t *g = agopen("G", Agdirected, NULL);
  Agnode_t *a = agnode(g, "a", 1);
  Agnode_t *b = agnode(g, "b", 1);
  Agnode_t *c = agnode(g, "c", 1);
  Agnode_t *a2 = agnode(g, "a", 0);
  printf("find same %d contains %d kind %d graphof %s\n", a == a2, agcontains(g, a), agobjkind(a), agnameof(agraphof(a)));
  printf("missing %s\n", agnode(g, "missing", 0) ? "yes" : "null");
  walk_nodes("forward", g);
  printf("backward");
  for (Agnode_t *n = aglstnode(g); n; n = agprvnode(g, n)) printf(" %s", agnameof(n));
  printf("\n");
  printf("subrep %s\n", agsubrep(g, a) ? "yes" : "null");
  printf("nodebefore %d\n", agnodebefore(c, a));
  walk_nodes("after-before", g);

  printf("id find same %d create %s\n", agidnode(g, AGID(a), 0) == a, agidnode(g, 42, 1) ? "yes" : "null");

  /* agrelabel_node segfaults inside libcgraph 2.42.4. The symbol is still bound. */

  Agedge_t *e1 = agedge(g, a, b, "e", 1);
  Agedge_t *e2 = agedge(g, a, b, NULL, 1);
  Agedge_t *e1b = agedge(g, a, b, "e", 0);
  printf("edge same %d eq %d kind %d\n", e1 == e1b, ageqedge(e1, e2), agobjkind(e1));
  printf("ends %s -> %s\n", agnameof(agtail(e1)), agnameof(aghead(e1)));
  Agedge_t *opp = agopp(e1);
  printf("opp %s -> %s\n", agnameof(agtail(opp)), agnameof(aghead(opp)));
  printf("out");
  for (Agedge_t *e = agfstout(g, a); e; e = agnxtout(g, e)) {
    printf(" %s->%s:", agnameof(agtail(e)), agnameof(aghead(e)));
    print_str(agnameof(e));
  }
  printf("\nin");
  for (Agedge_t *e = agfstin(g, b); e; e = agnxtin(g, e)) {
    printf(" %s->%s:", agnameof(agtail(e)), agnameof(aghead(e)));
    print_str(agnameof(e));
  }
  printf("\nall");
  for (Agedge_t *e = agfstedge(g, a); e; e = agnxtedge(g, e, a)) {
    printf(" %s->%s", agnameof(agtail(e)), agnameof(aghead(e)));
  }
  printf("\n");
  printf("degree both %d in %d out %d uniq %d\n",
    agdegree(g, a, 1, 1), agdegree(g, a, 1, 0), agdegree(g, a, 0, 1), agcountuniqedges(g, a, 1, 1));
  printf("idedge find %d create %s\n",
    agidedge(g, a, b, AGID(e1), 0) == e1, agidedge(g, a, b, 7, 1) ? "yes" : "null");

  Agraph_t *strict = agopen("S", Agstrictdirected, NULL);
  Agnode_t *sn = agnode(strict, "n", 1);
  Agnode_t *sm = agnode(strict, "m", 1);
  Agedge_t *se1 = agedge(strict, sn, sm, NULL, 1);
  Agedge_t *se2 = agedge(strict, sn, sm, NULL, 1);
  printf("strict same %d edges %d\n", se1 == se2, agnedges(strict));
  agclose(strict);

  Agraph_t *sub = agsubg(g, "sub", 1);
  Agnode_t *sub_b = agsubnode(sub, b, 1);
  Agedge_t *sub_e = agsubedge(sub, e1, 1);
  printf("sub contains node %d edge %d\n", agcontains(sub, sub_b), agcontains(sub, sub_e));
  dump_graph("sub", sub);
  printf("sub parent %s\n", agnameof(agparent(sub)));
  printf("subgs");
  for (Agraph_t *sg = agfstsubg(g); sg; sg = agnxtsubg(sg)) printf(" %s", agnameof(sg));
  printf("\n");
  printf("idsub find %d create %s count %d\n",
    agidsubg(g, AGID(sub), 0) == sub, agidsubg(g, 9, 1) ? "yes" : "null", agnsubg(g));
  long deleted = agdelsubg(g, sub);
  int subgs_left = agnsubg(g);
  printf("delsub zero %d same %d count %d\n", deleted == 0, (Agraph_t *)deleted == sub, subgs_left);

  Agsym_t *rank = agattr(g, AGRAPH, "rankdir", "LR");
  Agsym_t *color = agattr(g, AGNODE, "color", "black");
  agattr(g, AGEDGE, "style", "solid");
  printf("attr %s def %s\n", rank->name, rank->defval);
  printf("safeset %d get ", agsafeset(a, "color", "red", ""));
  print_str(agget(a, "color"));
  printf("\nset %d get ", agset(a, "color", "blue"));
  print_str(agget(a, "color"));
  printf("\n");
  Agsym_t *sym = agattrsym(a, "color");
  printf("sym %s xget ", sym->name);
  print_str(agxget(a, sym));
  printf("\nxset %d get ", agxset(a, sym, "green"));
  print_str(agget(a, "color"));
  printf("\n");
  printf("nattr");
  for (Agsym_t *at = agnxtattr(g, AGNODE, NULL); at; at = agnxtattr(g, AGNODE, at)) {
    printf(" %s=", at->name);
    print_str(at->defval);
  }
  printf("\ncopy %d b ", agcopyattr(a, b));
  print_str(agget(b, "color"));
  printf("\nmissing-attr ");
  print_str(agget(a, "no-such"));
  printf("\n");
  (void)color;

  char *hello = agstrdup(g, "hello");
  char *hello2 = agstrbind(g, "hello");
  printf("strdup %s same %d\n", hello, hello == hello2);
  char *html = agstrdup_html(g, "<b>hi</b>");
  printf("htmlflag %d text %s plainflag %d\n", aghtmlstr(html), html, aghtmlstr(hello));
  printf("canonStr %s\n", agcanonStr("a b"));
  printf("canon %s\n", agcanon("a b", 0));
  char canon_buf[64];
  memset(canon_buf, 0, sizeof canon_buf);
  printf("strcanon %s\n", agstrcanon("a b", canon_buf));
  printf("strfree %d %d\n", agstrfree(g, hello), agstrfree(g, html));
  printf("bind-after ");
  print_str(agstrbind(g, "hello"));
  printf("\n");

  Agraph_t *io = agmemread("digraph Io { x -> y }");
  printf("memread ");
  dump_graph("io", io);
  slurp_write(io);
  Agraph_t *concat = agmemconcat(io, "digraph Io { y -> z }");
  printf("memconcat %s\n", concat ? "yes" : "null");
  slurp_write(io);

  char read_buf[] = "digraph R { p -> q }";
  FILE *mem = fmemopen(read_buf, strlen(read_buf), "r");
  Agraph_t *rd = agread(mem, NULL);
  fclose(mem);
  printf("agread ");
  dump_graph("rd", rd);
  char more[] = "digraph R { q -> r }";
  FILE *mem2 = fmemopen(more, strlen(more), "r");
  Agraph_t *cat = agconcat(rd, mem2, NULL);
  fclose(mem2);
  printf("agconcat %s edges %d\n", cat ? "yes" : "null", agnedges(rd));
  slurp_write(rd);

  agreadline(3);
  char filename[] = "oracle.gv";
  agsetfile(filename);
  Agraph_t *named = agmemread("digraph F { u -> v }");
  slurp_write(named);
  agclose(named);

  /* Count after the delete. One printf would evaluate the two calls in an unspecified order. */
  int edge_rc = agdeledge(g, e2);
  int edges_left = agnedges(g);
  printf("del edge %d edges %d\n", edge_rc, edges_left);
  printf("delete edge %d\n", agdelete(g, e1));
  int node_rc = agdelnode(g, c);
  int nodes_left = agnnodes(g);
  printf("del node %d nodes %d\n", node_rc, nodes_left);
  printf("contains c %d\n", agcontains(g, c));

  void *rec = agbindrec(b, "mine", 64, 1);
  printf("bindrec %s\n", rec ? "yes" : "null");
  ((int *)((char *)rec + sizeof(Agrec_t)))[0] = 12345;
  Agrec_t *got = aggetrec(b, "mine", 0);
  printf("getrec %d\n", got ? ((int *)((char *)got + sizeof(Agrec_t)))[0] : -1);
  printf("delrec %d\n", agdelrec(b, "mine"));
  printf("getrec2 %s\n", aggetrec(b, "mine", 0) ? "yes" : "null");
  aginit(g, AGNODE, "userrec", 32, 1);
  printf("init %s\n", aggetrec(b, "userrec", 0) ? "yes" : "null");
  agclean(g, AGNODE, "userrec");
  printf("clean %s\n", aggetrec(b, "userrec", 0) ? "yes" : "null");

  unsigned char *block = agalloc(g, 16);
  printf("alloc %s\n", block ? "yes" : "null");
  memset(block, 0xab, 16);
  unsigned char *grown = agrealloc(g, block, 16, 32);
  printf("realloc %d heap %s\n", grown ? grown[0] : -1, agheap(g) ? "yes" : "null");
  agfree(g, grown);

  agflatten(g, 1);
  walk_nodes("flat", g);
  agflatten(g, 0);
  walk_nodes("unflat", g);
  aginternalmapclearlocalnames(g);
  printf("cleared\n");

  int prev_level = agseterr(AGERR);
  printf("seterr prev %d\n", prev_level);
  agusererrf old = agseterrf(errfn);
  printf("err-start\n");
  int err_rc = agerr(AGERR, "plain message");
  printf("agerr %d\n", err_rc);
  int err_rc2 = agerr(AGERR, "hello %s %d", "x", 7);
  printf("agerr2 %d\n", err_rc2);
  agerrorf("error %s", "one");
  agwarningf("warn %s", "two");
  printf("err-end lasterr=");
  print_str(aglasterr());
  int err_count = agerrors();
  printf(" errors %d\n", err_count);
  int reset = agreseterrors();
  int after_reset = agerrors();
  printf("reset %d errors %d\n", reset, after_reset);
  agseterrf(old);
  agseterr(prev_level);

  int prev_cb = agcallbacks(g, 1);
  printf("callbacks prev %d\n", prev_cb);
  Agcbdisc_t disc;
  memset(&disc, 0, sizeof disc);
  disc.node.ins = on_ins;
  disc.node.mod = on_mod;
  disc.node.del = on_del;
  agpushdisc(g, &disc, NULL);
  agnode(g, "d", 1);
  printf("inserts %d pop %d\n", inserts, agpopdisc(g, &disc));
  agnode(g, "e", 1);
  printf("inserts-after %d\n", inserts);

  agclose(g);
  agclose(io);
  agclose(rd);

  GVC_t *gvc = gvContext();
  printf("version %s\n", gvcVersion(gvc));
  printf("date %s\n", gvcBuildDate(gvc));
  char **info = gvcInfo(gvc);
  printf("info %s | %s | %s\n", info[0], info[1], info[2]);
  int sz = 0;
  char **layouts = gvPluginList(gvc, "layout", &sz, "");
  printf("layouts %d\n", sz);
  for (int i = 0; i < sz; i++) {
    printf(" layout %s\n", layouts[i]);
    free(layouts[i]);
  }
  free(layouts);
  sz = 0;
  char **renders = gvPluginList(gvc, "render", &sz, "");
  printf("renders %d\n", sz);
  for (int i = 0; i < sz; i++) {
    printf(" render %s\n", renders[i]);
    free(renders[i]);
  }
  free(renders);

  GVC_t *clone = gvCloneGVC(gvc);
  printf("clone %s\n", gvcVersion(clone));
  gvFreeCloneGVC(clone);
  GVC_t *fresh = gvNEWcontext(NULL, 1);
  printf("newcontext %s\n", gvcVersion(fresh));
  gvFreeContext(fresh);
  GVC_t *plugins = gvContextPlugins(NULL, 0);
  printf("contextplugins %s\n", gvcVersion(plugins));
  gvFreeContext(plugins);
  graph_t *pg = gvPluginsGraph(gvc);
  printf("pluginsGraph %s\n", pg ? "yes" : "null");

  graph_t *layout_g = agmemread("digraph { a -> b; b -> c }");
  int layout_rc = gvLayout(gvc, layout_g, "dot");
  printf("layout %d\n", layout_rc);
  attach_attrs(layout_g);
  printf("attached\n");
  slurp_write(layout_g);

  char *rbuf = NULL;
  size_t rsize = 0;
  FILE *rf = open_memstream(&rbuf, &rsize);
  int render_rc = gvRender(gvc, layout_g, "plain", rf);
  fclose(rf);
  printf("render %d bytes %zu\n", render_rc, rsize);
  fwrite(rbuf, 1, rsize, stdout);
  if (rsize == 0 || rbuf[rsize - 1] != '\n') printf("\n");
  free(rbuf);

  char *data = NULL;
  unsigned int dlen = 0;
  int data_rc = gvRenderData(gvc, layout_g, "plain", &data, &dlen);
  printf("renderData %d bytes %u\n", data_rc, dlen);
  fwrite(data, 1, dlen, stdout);
  if (dlen == 0 || data[dlen - 1] != '\n') printf("\n");
  gvFreeRenderData(data);

  char out_path[] = "/tmp/jnr-graphviz-oracle.plain";
  int file_rc = gvRenderFilename(gvc, layout_g, "plain", out_path);
  printf("renderFilename %d\n", file_rc);
  FILE *out = fopen(out_path, "r");
  slurp_file(out);
  fclose(out);
  remove(out_path);
  printf("ENDFILE\n");

  printf("freeLayout %d\n", gvFreeLayout(gvc, layout_g));
  agclose(layout_g);

  graph_t *tred_g = agmemread("digraph { a -> b; b -> c; a -> c }");
  printf("tred before %d\n", agnedges(tred_g));
  int tred_rc = gvToolTred(tred_g);
  int tred_edges = agnedges(tred_g);
  printf("tred %d after %d\n", tred_rc, tred_edges);
  slurp_write(tred_g);
  agclose(tred_g);

  graph_t *parts = agmemread("graph { a -- b; c -- d }");
  aginit(parts, AGRAPH, "Agraphinfo_t", sizeof(Agraphinfo_t), 1);
  aginit(parts, AGNODE, "Agnodeinfo_t", sizeof(Agnodeinfo_t), 1);
  printf("connected %d\n", isConnected(parts));
  graph_t *one = agmemread("graph { a -- b -- c }");
  aginit(one, AGRAPH, "Agraphinfo_t", sizeof(Agraphinfo_t), 1);
  aginit(one, AGNODE, "Agnodeinfo_t", sizeof(Agnodeinfo_t), 1);
  printf("connected-one %d\n", isConnected(one));

  int ncc = -1;
  graph_t **cc = ccomps(parts, &ncc, NULL);
  printf("ccomps %d\n", ncc);
  for (int i = 0; i < ncc; i++) {
    printf(" cc %s nodes %d edges %d\n", agnameof(cc[i]), agnnodes(cc[i]), agnedges(cc[i]));
    printf(" induce %d\n", nodeInduce(cc[i]));
    printf(" induced nodes %d edges %d\n", agnnodes(cc[i]), agnedges(cc[i]));
  }
  free(cc);

  int ncc2 = -1;
  graph_t **cc2 = cccomps(parts, &ncc2, NULL);
  printf("cccomps %d\n", ncc2);
  free(cc2);

  int ncc3 = -1;
  boolean changed = 2;
  graph_t **cc3 = pccomps(parts, &ncc3, NULL, &changed);
  printf("pccomps %d changed %d\n", ncc3, (int)changed);
  free(cc3);

  printf("packmode %d pack %d\n", (int)getPackMode(parts, 0), getPack(parts, -1, 8));

  unsigned char info_bytes[sizeof(pack_info)];
  memset(info_bytes, 0x5a, sizeof info_bytes);
  pack_info *pinfo = (pack_info *)info_bytes;
  int mode = (int)parsePackModeInfo("array_3", l_undef, pinfo);
  printf("parse %d\n", mode);
  for (size_t i = 0; i < sizeof info_bytes; i++) printf("%02x", info_bytes[i]);
  printf("\n");

  agclose(parts);
  agclose(one);

  GVC_t *extra = gvContext();
  gvFinalize(extra);
  printf("finalize %d\n", gvFreeContext(extra));
  gvToggle(0);
  printf("toggle\n");
  printf("context %d\n", gvFreeContext(gvc));
  return 0;
}
