import io.github.igrqb.jnr.graphviz.Graphviz;
import io.github.igrqb.jnr.graphviz.OutputFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class TestDotExport {
  private static final Logger log = LoggerFactory.getLogger(TestDotExport.class);

  private static final OutputFormat[] formatsToTest = {
      OutputFormat.BMP,
      OutputFormat.JPG,
      OutputFormat.PDF,
      OutputFormat.PNG,
      OutputFormat.SVG
  };

  private static Stream<OutputFormat> testProvider() {
    return Arrays.stream(formatsToTest);
  }

  @ParameterizedTest
  @MethodSource("testProvider")
  public void testDotExport(OutputFormat format) throws IOException {
    log.info("Testing {} ({})", format.getFormat(), format.getDescription());

    String dot = "digraph { a -> b; b -> c }";
    byte[] output = Graphviz.export(dot, format);

    BufferedImage expected = format == OutputFormat.PDF ?
        TestUtils.pdfToImage(Objects.requireNonNull(this.getClass().getResourceAsStream("expected." + format.getCommandLineParameter())), 300) :
        ImageIO.read(Objects.requireNonNull(this.getClass().getResourceAsStream("expected." + format.getCommandLineParameter())));

    BufferedImage actual = format == OutputFormat.PDF ?
        TestUtils.pdfToImage(new ByteArrayInputStream(output), 300) :
        ImageIO.read(new ByteArrayInputStream(output));

    Assertions.assertEquals(expected.getHeight(), actual.getHeight(), "Image heights differ");
    Assertions.assertEquals(expected.getWidth(), actual.getWidth(), "Image widths differ");
    Assertions.assertTrue(TestUtils.compareImages(expected, actual, 0.05, false));
  }
}
