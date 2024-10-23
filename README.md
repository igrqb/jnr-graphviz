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
  <version>0.5</version>
</dependency>
```

#### Gradle

```groovy
implementation 'io.github.igrqb:jnr-graphviz:0.5'
```

For more dependency systems: https://mvnrepository.com/artifact/io.github.igrqb/jnr-graphviz/


#### Usage

```java
import io.github.igrqb.jnr.graphviz.Graphviz;

String dot = "digraph { a -> b; b -> c }";
String svg = Graphviz.dotToSvg(dot);

// or directly to file
File svgFile = Graphviz.dotToSvg(dot, "/path/to/file.svg");
```
