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
