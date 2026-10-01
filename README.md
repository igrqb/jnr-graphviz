# jnr-graphviz
### Java bindings for simple Graphviz commands using JNR

Expects Graphviz to be installed on the target system, e.g.:

* Ubuntu: `sudo apt install graphviz libgraphviz-dev`
* Fedora: `sudo dnf in graphviz graphviz-devel`
* etc.

### Add dependency

#### Maven

```xml
<dependency>
  <groupId>io.github.igrqb</groupId>
  <artifactId>jnr-graphviz</artifactId>
  <version>0.5.2</version>
</dependency>
```

#### Gradle

```groovy
implementation 'io.github.igrqb:jnr-graphviz:0.5.2'
```

For more dependency systems: https://mvnrepository.com/artifact/io.github.igrqb/jnr-graphviz/


#### Usage

```java
import io.github.igrqb.jnr.graphviz.Graphviz;

String dot = "digraph { a -> b; b -> c }";
String svg = Graphviz.dotToSvg(dot);

// or directly to file
File svgFile = Graphviz.dotToSvg(dot, "/path/to/file.svg");

// Other export formats
OutputFormat format = OutputFormat.BMP;
// OutputFormat.JPG
// OutputFormat.PDF
// OutputFormat.PNG
// OutputFormat.SVG
// See OutputFormat.java for more
byte[] output = Graphviz.export(dot, format);
```

### simple.c

[Graphviz `dot.demo/simple.c`](https://gitlab.com/graphviz/graphviz/-/blob/main/dot.demo/simple.c) reads a graph, lays it out with `dot`, and renders `plain` text. The copies here are `src/test/c/dot_simple.c` and `ExamplePorts.simple`. `TestCExamples` compiles the C program against the installed `libgvc` and `libcgraph`, runs both sides on `src/test/resources/simple.gv`, and checks the bytes. On Graphviz 2.42.4 that file is a 385-byte plain graph from both.

The C program renders to `stdout`. Java renders into `NativeGraphviz.MemStream`, an `open_memstream` `FILE`, and returns that buffer. `gvContext`, `gvLayout`, `gvRender`, `gvFreeLayout`, `agclose`, and `gvFreeContext` are the same calls. The Java helper closes the `FILE` after `agread` returns. The C demo leaves it open until the process exits. The second argument of `agread` is `null`, which selects the default discipline.

Input, `src/test/resources/simple.gv`:

```
digraph G {
  label="simple";
  a -> b;
  b -> c;
}
```

C, `src/test/c/dot_simple.c`:

```c
#include <graphviz/gvc.h>
#include <stddef.h>
#include <stdio.h>

int main(int argc, char **argv) {
  GVC_t *gvc = gvContext();

  FILE *fp;
  if (argc > 1)
    fp = fopen(argv[1], "r");
  else
    fp = stdin;
  graph_t *g = agread(fp, NULL);

  gvLayout(gvc, g, "dot");
  gvRender(gvc, g, "plain", stdout);
  gvFreeLayout(gvc, g);
  agclose(g);

  return gvFreeContext(gvc);
}
```

Java, the body of `ExamplePorts.simple`:

```java
import io.github.igrqb.jnr.graphviz.LibC;
import io.github.igrqb.jnr.graphviz.LibCgraph;
import io.github.igrqb.jnr.graphviz.LibGvc;
import io.github.igrqb.jnr.graphviz.NativeGraphviz;
import jnr.ffi.Pointer;

public final class Simple {
  private static final LibC libc = NativeGraphviz.libc;
  private static final LibCgraph cgraph = NativeGraphviz.cgraph;
  private static final LibGvc gvc = NativeGraphviz.gvc;

  public static byte[] simple(String path) {
    Pointer context = gvc.gvContext();
    Pointer graph = readFile(path);
    try {
      gvc.gvLayout(context, graph, "dot");
      try (NativeGraphviz.MemStream stream = new NativeGraphviz.MemStream()) {
        gvc.gvRender(context, graph, "plain", stream.file());
        return stream.toByteArray();
      }
    } finally {
      gvc.gvFreeLayout(context, graph);
      cgraph.agclose(graph);
      gvc.gvFreeContext(context);
    }
  }

  private static Pointer readFile(String path) {
    Pointer file = libc.fopen(path, "r");
    if (file == null || file.address() == 0) {
      throw new IllegalStateException("fopen " + path);
    }
    try {
      Pointer graph = cgraph.agread(file, null);
      if (graph == null || graph.address() == 0) {
        throw new IllegalStateException("agread " + path);
      }
      return graph;
    } finally {
      libc.fclose(file);
    }
  }
}
```

| C | Java |
| --- | --- |
| `gvContext()` | `gvc.gvContext()` |
| `fopen` + `agread(fp, NULL)` | `libc.fopen` + `cgraph.agread(file, null)` |
| `gvLayout(gvc, g, "dot")` | `gvc.gvLayout(context, graph, "dot")` |
| `gvRender(gvc, g, "plain", stdout)` | `gvc.gvRender(context, graph, "plain", stream.file())` |
| `gvFreeLayout`, `agclose`, `gvFreeContext` | the same three calls in `finally` |

### Publish

Publishing uses the [JReleaser](https://jreleaser.org) CLI, version 1.26. Install it with SDKMAN:

```bash
sdk install jreleaser 1.26.0
```

Copy `jreleaser-template.yml` to `jreleaser.yml` and fill in the placeholders described in that file. `jreleaser.yml` is gitignored.

From the repository root, stage the artifacts and check the deploy before uploading:

```bash
./gradlew clean publish
jreleaser deploy --dry-run
jreleaser deploy
```

`publish` writes the jar, sources jar, javadoc jar, and POM into `build/staging-deploy`, and signs them with the local GnuPG key. `jreleaser deploy` uploads that directory to the Central Publisher Portal and publishes it. The command waits until the deployment reaches `PUBLISHED`. Progress is at https://central.sonatype.com/publishing/deployments.

To upload without publishing, then publish after checking the portal:

```bash
JRELEASER_MAVENCENTRAL_STAGE=UPLOAD jreleaser deploy
JRELEASER_MAVENCENTRAL_STAGE=PUBLISH jreleaser deploy
```

Set `JRELEASER_MAVENCENTRAL_SONATYPE_DEPLOYMENT_ID` to the deployment id written to `out/jreleaser/output.properties` by the upload. Maven Central rejects a repeated version, so bump `version` in `build.gradle` and `project.version` in `jreleaser.yml` together before another release.
