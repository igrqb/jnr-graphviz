import io.github.igrqb.jnr.graphviz.Graphviz;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class TestDotToSvg {
  private static final Logger log = LoggerFactory.getLogger(TestDotToSvg.class);

  @Test
  public void testSimpleDotToSvg() throws IOException {
    String dot = "digraph { a -> b; b -> c }";

    String svg = Graphviz.dotToSvg(dot);

    BufferedImage expected = ImageIO.read(Objects.requireNonNull(this.getClass().getResourceAsStream("expected.svg")));
    BufferedImage actual = ImageIO.read(new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8)));

    Assertions.assertEquals(expected.getHeight(), actual.getHeight(), "Image heights differ");
    Assertions.assertEquals(expected.getWidth(), actual.getWidth(), "Image widths differ");
    Assertions.assertTrue(TestUtils.compareImages(expected, actual, 0.05, false));
  }

  @Test
  public void testDotToSvgFile() throws IOException {
    String dot = "digraph { a -> b; b -> c }";

    String svg = Graphviz.dotToSvg(dot);

    File svgFile = Graphviz.dotToSvg(dot, "/tmp/graph.svg");

    String fromFile = FileUtils.readFileToString(svgFile, StandardCharsets.UTF_8);

    Assertions.assertEquals(svg, fromFile);
  }
}
