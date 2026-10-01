import io.github.igrqb.jnr.graphviz.AgCallbacks;
import io.github.igrqb.jnr.graphviz.Agdesc;
import io.github.igrqb.jnr.graphviz.Agerrlevel;
import io.github.igrqb.jnr.graphviz.Agobj;
import io.github.igrqb.jnr.graphviz.AgobjKind;
import io.github.igrqb.jnr.graphviz.Agsym;
import io.github.igrqb.jnr.graphviz.LibC;
import io.github.igrqb.jnr.graphviz.LibCgraph;
import io.github.igrqb.jnr.graphviz.LibGvc;
import io.github.igrqb.jnr.graphviz.LibPack;
import io.github.igrqb.jnr.graphviz.NativeGraphviz;
import io.github.igrqb.jnr.graphviz.PackMode;
import jnr.ffi.LibraryLoader;
import jnr.ffi.Memory;
import jnr.ffi.Pointer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Same sequence of libcgraph, libgvc, and pack calls as {@code src/test/c/api_oracle.c}.
 * The transcript is the bytes that program writes to stdout.
 */
public final class ApiOracle {
  private static final LibC libc = NativeGraphviz.libc;
  private static final LibCgraph cgraph = NativeGraphviz.cgraph;
  private static final LibGvc gvc = NativeGraphviz.gvc;
  private static final LibPack pack = NativeGraphviz.pack;
  private static final int SIGUSR1 = 10;
  /** Bytes in front of a canon argument so {@code aghtmlstr} does not see a malloc header. */
  private static final int CANON_PREFIX = 64;
  private static final CSignals signals = LibraryLoader.create(CSignals.class).load("c");

  /** {@code signal(2)}, used to put SIGUSR1 back after {@code gvToggle}. */
  public interface CSignals {
    Pointer signal(int signum, Pointer handler);
  }

  private ApiOracle() {
  }

  /**
   * Print the transcript to stdout.
   * A fresh process is required: cgraph names anonymous edges from a process-wide counter.
   */
  public static void main(String[] args) throws Exception {
    byte[] bytes = transcript(GraphvizC.sizes());
    System.out.write(bytes);
    System.out.flush();
  }

  /**
   * Run the oracle.
   * Error reporting is cleared first so a previous call in this process does not change the counts.
   * The SIGUSR1 handler installed by {@code gvToggle} is removed before returning.
   */
  public static byte[] transcript(GraphvizC.Sizes sizes) {
    cgraph.agseterr(Agerrlevel.WARN);
    cgraph.agreseterrors();
    Transcript t = new Transcript();
    List<Object> pins = new ArrayList<>();
    try {
      t.printf("Agdirected %d\n", Agdesc.directed());
      t.printf("Agstrictdirected %d\n", Agdesc.strictDirected());
      t.printf("Agundirected %d\n", Agdesc.undirected());
      t.printf("Agstrictundirected %d\n", Agdesc.strictUndirected());

      int[] descriptors = {
        Agdesc.directed(), Agdesc.strictDirected(), Agdesc.undirected(), Agdesc.strictUndirected()
      };
      String[] kindNames = {"directed", "strictdirected", "undirected", "strictundirected"};
      for (int i = 0; i < descriptors.length; i++) {
        Pointer opened = cgraph.agopen(kindNames[i], descriptors[i], null);
        dump(t, kindNames[i], opened);
        Pointer parent = cgraph.agparent(opened);
        t.print("parent " + (alive(parent) ? cgraph.agnameof(parent) : "null") + "\n");
        cgraph.agclose(opened);
      }

      int built = Agdesc.of(Agdesc.DIRECTED, Agdesc.MAINGRAPH);
      Pointer builtGraph = cgraph.agopen("built", built, null);
      dump(t, "built", builtGraph);
      cgraph.agclose(builtGraph);

      Pointer graph = cgraph.agopen("G", Agdesc.directed(), null);
      Pointer a = cgraph.agnode(graph, "a", 1);
      Pointer b = cgraph.agnode(graph, "b", 1);
      Pointer c = cgraph.agnode(graph, "c", 1);
      Pointer a2 = cgraph.agnode(graph, "a", 0);
      t.printf("find same %d contains %d kind %d graphof %s\n",
        identical(a, a2), cgraph.agcontains(graph, a), cgraph.agobjkind(a), cgraph.agnameof(cgraph.agraphof(a)));
      t.print("missing " + (alive(cgraph.agnode(graph, "missing", 0)) ? "yes" : "null") + "\n");
      walk(t, "forward", graph);
      t.print("backward");
      for (Pointer n = cgraph.aglstnode(graph); alive(n); n = cgraph.agprvnode(graph, n)) {
        t.print(" " + cgraph.agnameof(n));
      }
      t.print("\n");
      t.print("subrep " + (alive(cgraph.agsubrep(graph, a)) ? "yes" : "null") + "\n");
      t.printf("nodebefore %d\n", cgraph.agnodebefore(c, a));
      walk(t, "after-before", graph);

      Pointer foundById = cgraph.agidnode(graph, Agobj.id(a), 0);
      Pointer createdById = cgraph.agidnode(graph, 42, 1);
      t.printf("id find same %d create %s\n", identical(foundById, a), alive(createdById) ? "yes" : "null");

      Pointer e1 = cgraph.agedge(graph, a, b, "e", 1);
      Pointer e2 = cgraph.agedge(graph, a, b, null, 1);
      Pointer e1b = cgraph.agedge(graph, a, b, "e", 0);
      t.printf("edge same %d eq %d kind %d\n", identical(e1, e1b), cgraph.ageqedge(e1, e2), cgraph.agobjkind(e1));
      t.printf("ends %s -> %s\n", cgraph.agnameof(cgraph.agtail(e1)), cgraph.agnameof(cgraph.aghead(e1)));
      Pointer opposite = cgraph.agopp(e1);
      t.printf("opp %s -> %s\n", cgraph.agnameof(cgraph.agtail(opposite)), cgraph.agnameof(cgraph.aghead(opposite)));
      t.print("out");
      for (Pointer e = cgraph.agfstout(graph, a); alive(e); e = cgraph.agnxtout(graph, e)) {
        t.print(" " + cgraph.agnameof(cgraph.agtail(e)) + "->" + cgraph.agnameof(cgraph.aghead(e)) + ":");
        t.print(text(cgraph.agnameof(e)));
      }
      t.print("\nin");
      for (Pointer e = cgraph.agfstin(graph, b); alive(e); e = cgraph.agnxtin(graph, e)) {
        t.print(" " + cgraph.agnameof(cgraph.agtail(e)) + "->" + cgraph.agnameof(cgraph.aghead(e)) + ":");
        t.print(text(cgraph.agnameof(e)));
      }
      t.print("\nall");
      for (Pointer e = cgraph.agfstedge(graph, a); alive(e); e = cgraph.agnxtedge(graph, e, a)) {
        t.print(" " + cgraph.agnameof(cgraph.agtail(e)) + "->" + cgraph.agnameof(cgraph.aghead(e)));
      }
      t.print("\n");
      t.printf("degree both %d in %d out %d uniq %d\n",
        cgraph.agdegree(graph, a, 1, 1), cgraph.agdegree(graph, a, 1, 0),
        cgraph.agdegree(graph, a, 0, 1), cgraph.agcountuniqedges(graph, a, 1, 1));
      Pointer foundEdge = cgraph.agidedge(graph, a, b, Agobj.id(e1), 0);
      Pointer createdEdge = cgraph.agidedge(graph, a, b, 7, 1);
      t.printf("idedge find %d create %s\n", identical(foundEdge, e1), alive(createdEdge) ? "yes" : "null");

      Pointer strict = cgraph.agopen("S", Agdesc.strictDirected(), null);
      Pointer sn = cgraph.agnode(strict, "n", 1);
      Pointer sm = cgraph.agnode(strict, "m", 1);
      Pointer se1 = cgraph.agedge(strict, sn, sm, null, 1);
      Pointer se2 = cgraph.agedge(strict, sn, sm, null, 1);
      t.printf("strict same %d edges %d\n", identical(se1, se2), cgraph.agnedges(strict));
      cgraph.agclose(strict);

      Pointer sub = cgraph.agsubg(graph, "sub", 1);
      Pointer subNode = cgraph.agsubnode(sub, b, 1);
      Pointer subEdge = cgraph.agsubedge(sub, e1, 1);
      t.printf("sub contains node %d edge %d\n", cgraph.agcontains(sub, subNode), cgraph.agcontains(sub, subEdge));
      dump(t, "sub", sub);
      t.printf("sub parent %s\n", cgraph.agnameof(cgraph.agparent(sub)));
      t.print("subgs");
      for (Pointer sg = cgraph.agfstsubg(graph); alive(sg); sg = cgraph.agnxtsubg(sg)) {
        t.print(" " + cgraph.agnameof(sg));
      }
      t.print("\n");
      Pointer foundSub = cgraph.agidsubg(graph, Agobj.id(sub), 0);
      Pointer createdSub = cgraph.agidsubg(graph, 9, 1);
      int subCount = cgraph.agnsubg(graph);
      t.printf("idsub find %d create %s count %d\n",
        identical(foundSub, sub), alive(createdSub) ? "yes" : "null", subCount);
      long deleted = cgraph.agdelsubg(graph, sub);
      int subgsLeft = cgraph.agnsubg(graph);
      t.printf("delsub zero %d same %d count %d\n",
        deleted == 0 ? 1 : 0, deleted == sub.address() ? 1 : 0, subgsLeft);

      Pointer rank = cgraph.agattr(graph, AgobjKind.GRAPH, "rankdir", "LR");
      cgraph.agattr(graph, AgobjKind.NODE, "color", "black");
      cgraph.agattr(graph, AgobjKind.EDGE, "style", "solid");
      t.printf("attr %s def %s\n", Agsym.name(rank), Agsym.defval(rank));
      t.printf("safeset %d get ", cgraph.agsafeset(a, "color", "red", ""));
      t.print(text(cgraph.agget(a, "color")));
      t.printf("\nset %d get ", cgraph.agset(a, "color", "blue"));
      t.print(text(cgraph.agget(a, "color")));
      t.print("\n");
      Pointer sym = cgraph.agattrsym(a, "color");
      t.printf("sym %s xget ", Agsym.name(sym));
      t.print(text(cgraph.agxget(a, sym)));
      t.printf("\nxset %d get ", cgraph.agxset(a, sym, "green"));
      t.print(text(cgraph.agget(a, "color")));
      t.print("\n");
      t.print("nattr");
      for (Pointer attr = cgraph.agnxtattr(graph, AgobjKind.NODE, null); alive(attr); attr = cgraph.agnxtattr(graph, AgobjKind.NODE, attr)) {
        t.print(" " + Agsym.name(attr) + "=");
        t.print(text(Agsym.defval(attr)));
      }
      t.printf("\ncopy %d b ", cgraph.agcopyattr(a, b));
      t.print(text(cgraph.agget(b, "color")));
      t.print("\nmissing-attr ");
      t.print(text(cgraph.agget(a, "no-such")));
      t.print("\n");

      Pointer hello = cgraph.agstrdup(graph, "hello");
      Pointer hello2 = cgraph.agstrbind(graph, "hello");
      t.printf("strdup %s same %d\n", NativeGraphviz.readString(hello), identical(hello, hello2));
      Pointer html = cgraph.agstrdup_html(graph, "<b>hi</b>");
      t.printf("htmlflag %d text %s plainflag %d\n",
        cgraph.aghtmlstr(html), NativeGraphviz.readString(html), cgraph.aghtmlstr(hello));
      Pointer canonText = canonLiteral("a b");
      pins.add(canonText);
      Pointer canonArg = canonText.slice(CANON_PREFIX);
      t.printf("canonStr %s\n", cgraph.agcanonStr(canonArg));
      t.printf("canon %s\n", cgraph.agcanon("a b", 0));
      Pointer canonBuf = Memory.allocateDirect(NativeGraphviz.runtime, 64);
      pins.add(canonBuf);
      canonBuf.setMemory(0, 64, (byte) 0);
      t.printf("strcanon %s\n", NativeGraphviz.readString(cgraph.agstrcanon(canonArg, canonBuf)));
      int freedHello = cgraph.agstrfree(graph, hello);
      int freedHtml = cgraph.agstrfree(graph, html);
      t.printf("strfree %d %d\n", freedHello, freedHtml);
      t.print("bind-after ");
      t.print(text(NativeGraphviz.readString(cgraph.agstrbind(graph, "hello"))));
      t.print("\n");

      Pointer io = cgraph.agmemread("digraph Io { x -> y }");
      t.print("memread ");
      dump(t, "io", io);
      slurpWrite(t, io);
      Pointer concat = cgraph.agmemconcat(io, "digraph Io { y -> z }");
      t.print("memconcat " + (alive(concat) ? "yes" : "null") + "\n");
      slurpWrite(t, io);

      MemFile readFile = new MemFile("digraph R { p -> q }");
      pins.add(readFile);
      Pointer rd = cgraph.agread(readFile.file, null);
      readFile.close();
      t.print("agread ");
      dump(t, "rd", rd);
      MemFile moreFile = new MemFile("digraph R { q -> r }");
      pins.add(moreFile);
      Pointer cat = cgraph.agconcat(rd, moreFile.file, null);
      moreFile.close();
      t.printf("agconcat %s edges %d\n", alive(cat) ? "yes" : "null", cgraph.agnedges(rd));
      slurpWrite(t, rd);

      cgraph.agreadline(3);
      Pointer filename = NativeGraphviz.stableCString("oracle.gv");
      pins.add(filename);
      cgraph.agsetfile(filename);
      Pointer named = cgraph.agmemread("digraph F { u -> v }");
      slurpWrite(t, named);
      cgraph.agclose(named);

      t.printf("del edge %d edges %d\n", cgraph.agdeledge(graph, e2), cgraph.agnedges(graph));
      t.printf("delete edge %d\n", cgraph.agdelete(graph, e1));
      t.printf("del node %d nodes %d\n", cgraph.agdelnode(graph, c), cgraph.agnnodes(graph));
      t.printf("contains c %d\n", cgraph.agcontains(graph, c));

      Pointer record = cgraph.agbindrec(b, "mine", 64, 1);
      t.print("bindrec " + (alive(record) ? "yes" : "null") + "\n");
      record.putInt(sizes.agrec, 12345);
      Pointer got = cgraph.aggetrec(b, "mine", 0);
      t.printf("getrec %d\n", alive(got) ? got.getInt(sizes.agrec) : -1);
      t.printf("delrec %d\n", cgraph.agdelrec(b, "mine"));
      t.print("getrec2 " + (alive(cgraph.aggetrec(b, "mine", 0)) ? "yes" : "null") + "\n");
      cgraph.aginit(graph, AgobjKind.NODE, "userrec", 32, 1);
      t.print("init " + (alive(cgraph.aggetrec(b, "userrec", 0)) ? "yes" : "null") + "\n");
      cgraph.agclean(graph, AgobjKind.NODE, "userrec");
      t.print("clean " + (alive(cgraph.aggetrec(b, "userrec", 0)) ? "yes" : "null") + "\n");

      Pointer block = cgraph.agalloc(graph, 16);
      t.print("alloc " + (alive(block) ? "yes" : "null") + "\n");
      block.setMemory(0, 16, (byte) 0xab);
      Pointer grown = cgraph.agrealloc(graph, block, 16, 32);
      String heap = alive(cgraph.agheap(graph)) ? "yes" : "null";
      t.printf("realloc %d heap %s\n", alive(grown) ? (grown.getByte(0) & 0xff) : -1, heap);
      cgraph.agfree(graph, grown);

      cgraph.agflatten(graph, 1);
      walk(t, "flat", graph);
      cgraph.agflatten(graph, 0);
      walk(t, "unflat", graph);
      cgraph.aginternalmapclearlocalnames(graph);
      t.print("cleared\n");

      reportErrors(t, graph);
      cgraph.agclose(graph);
      cgraph.agclose(io);
      cgraph.agclose(rd);

      Pointer context = gvc.gvContext();
      t.printf("version %s\n", gvc.gvcVersion(context));
      t.printf("date %s\n", gvc.gvcBuildDate(context));
      String[] info = NativeGraphviz.info(context);
      t.printf("info %s | %s | %s\n", info[0], info[1], info[2]);
      List<String> layouts = NativeGraphviz.plugins(context, "layout");
      t.printf("layouts %d\n", layouts.size());
      for (String name : layouts) {
        t.printf(" layout %s\n", name);
      }
      List<String> renders = NativeGraphviz.plugins(context, "render");
      t.printf("renders %d\n", renders.size());
      for (String name : renders) {
        t.printf(" render %s\n", name);
      }

      Pointer clone = gvc.gvCloneGVC(context);
      t.printf("clone %s\n", gvc.gvcVersion(clone));
      gvc.gvFreeCloneGVC(clone);
      Pointer fresh = gvc.gvNEWcontext(null, 1);
      t.printf("newcontext %s\n", gvc.gvcVersion(fresh));
      gvc.gvFreeContext(fresh);
      Pointer plugins = gvc.gvContextPlugins(null, 0);
      t.printf("contextplugins %s\n", gvc.gvcVersion(plugins));
      gvc.gvFreeContext(plugins);
      Pointer pluginsGraph = gvc.gvPluginsGraph(context);
      t.print("pluginsGraph " + (alive(pluginsGraph) ? "yes" : "null") + "\n");

      Pointer layoutGraph = cgraph.agmemread("digraph { a -> b; b -> c }");
      int layoutRc = gvc.gvLayout(context, layoutGraph, "dot");
      t.printf("layout %d\n", layoutRc);
      gvc.attach_attrs(layoutGraph);
      t.print("attached\n");
      slurpWrite(t, layoutGraph);

      try (NativeGraphviz.MemStream stream = new NativeGraphviz.MemStream()) {
        int renderRc = gvc.gvRender(context, layoutGraph, "plain", stream.file());
        byte[] rendered = stream.toByteArray();
        t.printf("render %d bytes %d\n", renderRc, rendered.length);
        writeBlock(t, rendered);
      }

      byte[] renderedData = NativeGraphviz.renderData(context, layoutGraph, "plain");
      t.printf("renderData 0 bytes %d\n", renderedData.length);
      writeBlock(t, renderedData);

      String outPath = "/tmp/jnr-graphviz-oracle.plain";
      int fileRc = gvc.gvRenderFilename(context, layoutGraph, "plain", outPath);
      t.printf("renderFilename %d\n", fileRc);
      t.write(Files.readAllBytes(Path.of(outPath)));
      Files.deleteIfExists(Path.of(outPath));
      t.print("ENDFILE\n");

      t.printf("freeLayout %d\n", gvc.gvFreeLayout(context, layoutGraph));
      cgraph.agclose(layoutGraph);

      Pointer tredGraph = cgraph.agmemread("digraph { a -> b; b -> c; a -> c }");
      t.printf("tred before %d\n", cgraph.agnedges(tredGraph));
      int tredRc = gvc.gvToolTred(tredGraph);
      int tredEdges = cgraph.agnedges(tredGraph);
      t.printf("tred %d after %d\n", tredRc, tredEdges);
      slurpWrite(t, tredGraph);
      cgraph.agclose(tredGraph);

      Pointer parts = cgraph.agmemread("graph { a -- b; c -- d }");
      cgraph.aginit(parts, AgobjKind.GRAPH, "Agraphinfo_t", sizes.agraphinfo, 1);
      cgraph.aginit(parts, AgobjKind.NODE, "Agnodeinfo_t", sizes.agnodeinfo, 1);
      t.printf("connected %d\n", pack.isConnected(parts));
      Pointer one = cgraph.agmemread("graph { a -- b -- c }");
      cgraph.aginit(one, AgobjKind.GRAPH, "Agraphinfo_t", sizes.agraphinfo, 1);
      cgraph.aginit(one, AgobjKind.NODE, "Agnodeinfo_t", sizes.agnodeinfo, 1);
      t.printf("connected-one %d\n", pack.isConnected(one));

      Pointer count = Memory.allocateDirect(NativeGraphviz.runtime, Integer.BYTES);
      count.putInt(0, -1);
      Pointer components = pack.ccomps(parts, count, null);
      int ncc = count.getInt(0);
      t.printf("ccomps %d\n", ncc);
      for (int i = 0; i < ncc; i++) {
        Pointer component = components.getPointer(i * 8L);
        t.printf(" cc %s nodes %d edges %d\n",
          cgraph.agnameof(component), cgraph.agnnodes(component), cgraph.agnedges(component));
        t.printf(" induce %d\n", pack.nodeInduce(component));
        t.printf(" induced nodes %d edges %d\n", cgraph.agnnodes(component), cgraph.agnedges(component));
      }
      libc.free(components);

      count.putInt(0, -1);
      Pointer clusters = pack.cccomps(parts, count, null);
      t.printf("cccomps %d\n", count.getInt(0));
      libc.free(clusters);

      count.putInt(0, -1);
      Pointer changed = Memory.allocateDirect(NativeGraphviz.runtime, 1);
      changed.putByte(0, (byte) 2);
      Pointer packed = pack.pccomps(parts, count, null, changed);
      t.printf("pccomps %d changed %d\n", count.getInt(0), changed.getByte(0) & 0xff);
      libc.free(packed);

      t.printf("packmode %d pack %d\n", pack.getPackMode(parts, 0), pack.getPack(parts, -1, 8));
      Pointer infoBytes = Memory.allocateDirect(NativeGraphviz.runtime, sizes.packInfo);
      infoBytes.setMemory(0, sizes.packInfo, (byte) 0x5a);
      int mode = pack.parsePackModeInfo("array_3", PackMode.UNDEF, infoBytes);
      t.printf("parse %d\n", mode);
      for (int i = 0; i < sizes.packInfo; i++) {
        t.printf("%02x", infoBytes.getByte(i) & 0xff);
      }
      t.print("\n");

      cgraph.agclose(parts);
      cgraph.agclose(one);

      Pointer extra = gvc.gvContext();
      gvc.gvFinalize(extra);
      t.printf("finalize %d\n", gvc.gvFreeContext(extra));
      gvc.gvToggle(0);
      t.print("toggle\n");
      t.printf("context %d\n", gvc.gvFreeContext(context));
      return t.toByteArray();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } finally {
      signals.signal(SIGUSR1, null);
      pins.clear();
    }
  }

  private static void reportErrors(Transcript t, Pointer graph) {
    int prevLevel = cgraph.agseterr(Agerrlevel.ERR);
    t.printf("seterr prev %d\n", prevLevel);
    AgCallbacks.ErrorFn handler = message -> {
      t.print("ERRFN:" + text(message) + "\n");
      return 0;
    };
    Pointer previous = cgraph.agseterrf(handler);
    try {
      t.print("err-start\n");
      int errRc = cgraph.agerr(Agerrlevel.ERR, "plain message");
      t.printf("agerr %d\n", errRc);
      int errRc2 = cgraph.agerr(Agerrlevel.ERR, "hello %s %d", "x", 7);
      t.printf("agerr2 %d\n", errRc2);
      cgraph.agerrorf("error %s", "one");
      cgraph.agwarningf("warn %s", "two");
      t.print("err-end lasterr=");
      t.print(text(cgraph.aglasterr()));
      int errCount = cgraph.agerrors();
      t.printf(" errors %d\n", errCount);
      int reset = cgraph.agreseterrors();
      int afterReset = cgraph.agerrors();
      t.printf("reset %d errors %d\n", reset, afterReset);
    } finally {
      cgraph.agseterrf(previous);
      cgraph.agseterr(prevLevel);
    }

    int[] inserts = {0};
    AgCallbacks.ObjectFn onIns = (g, obj, state) -> {
      inserts[0]++;
      t.print("ins " + text(cgraph.agnameof(g)) + " " + text(cgraph.agnameof(obj)) + "\n");
    };
    AgCallbacks.UpdateFn onMod = (g, obj, state, sym) -> {
    };
    AgCallbacks.ObjectFn onDel = (g, obj, state) -> {
    };
    int prevCallbacks = cgraph.agcallbacks(graph, 1);
    t.printf("callbacks prev %d\n", prevCallbacks);
    AgCallbacks.Disc disc = new AgCallbacks.Disc(NativeGraphviz.runtime);
    disc.nodeIns.set(onIns);
    disc.nodeMod.set(onMod);
    disc.nodeDel.set(onDel);
    cgraph.agpushdisc(graph, disc.pointer(), null);
    cgraph.agnode(graph, "d", 1);
    t.printf("inserts %d pop %d\n", inserts[0], cgraph.agpopdisc(graph, disc.pointer()));
    cgraph.agnode(graph, "e", 1);
    t.printf("inserts-after %d\n", inserts[0]);
  }

  private static void dump(Transcript t, String label, Pointer graph) {
    t.printf("%s name=%s directed=%d undirected=%d strict=%d simple=%d nodes=%d edges=%d subg=%d kind=%d root=%s\n",
      label,
      cgraph.agnameof(graph),
      cgraph.agisdirected(graph),
      cgraph.agisundirected(graph),
      cgraph.agisstrict(graph),
      cgraph.agissimple(graph),
      cgraph.agnnodes(graph),
      cgraph.agnedges(graph),
      cgraph.agnsubg(graph),
      cgraph.agobjkind(graph),
      cgraph.agnameof(cgraph.agroot(graph)));
  }

  private static void walk(Transcript t, String label, Pointer graph) {
    t.print(label);
    for (Pointer n = cgraph.agfstnode(graph); alive(n); n = cgraph.agnxtnode(graph, n)) {
      t.print(" " + cgraph.agnameof(n));
    }
    t.print("\n");
  }

  private static void slurpWrite(Transcript t, Pointer graph) {
    try (NativeGraphviz.MemStream stream = new NativeGraphviz.MemStream()) {
      int rc = cgraph.agwrite(graph, stream.file());
      byte[] bytes = stream.toByteArray();
      t.printf("agwrite %d bytes %d\n", rc, bytes.length);
      writeBlock(t, bytes);
    }
  }

  private static void writeBlock(Transcript t, byte[] bytes) {
    t.write(bytes);
    if (bytes.length == 0 || bytes[bytes.length - 1] != '\n') {
      t.print("\n");
    }
  }

  private static boolean alive(Pointer pointer) {
    return pointer != null && pointer.address() != 0L;
  }

  private static int identical(Pointer left, Pointer right) {
    long a = left == null ? 0L : left.address();
    long b = right == null ? 0L : right.address();
    return a == b ? 1 : 0;
  }

  private static String text(String value) {
    return value == null ? "null" : value;
  }

  /**
   * {@code agcanonStr} and {@code agstrcanon} call {@code aghtmlstr}, which reads the
   * refstr header in front of the pointer. A C string literal has no HTML bit there.
   * The returned block keeps {@link #CANON_PREFIX} zero bytes before the text.
   */
  private static Pointer canonLiteral(String text) {
    byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
    Pointer block = Memory.allocateDirect(NativeGraphviz.runtime, CANON_PREFIX + bytes.length + 1L);
    block.setMemory(0, CANON_PREFIX + bytes.length + 1L, (byte) 0);
    block.put(CANON_PREFIX, bytes, 0, bytes.length);
    return block;
  }

  private static final class MemFile {
    final Pointer buffer;
    final Pointer file;

    MemFile(String text) {
      byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
      buffer = Memory.allocateDirect(NativeGraphviz.runtime, bytes.length + 1L);
      buffer.put(0, bytes, 0, bytes.length);
      file = libc.fmemopen(buffer, bytes.length, "r");
    }

    void close() {
      libc.fclose(file);
    }
  }

  private static final class Transcript {
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    void print(String value) {
      out.writeBytes(value.getBytes(StandardCharsets.UTF_8));
    }

    void printf(String format, Object... args) {
      print(String.format(Locale.ROOT, format, args));
    }

    void write(byte[] bytes) {
      out.writeBytes(bytes);
    }

    byte[] toByteArray() {
      return out.toByteArray();
    }
  }
}
