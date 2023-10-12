import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.CvScalar;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_java;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_highgui.imshow;
import static org.bytedeco.opencv.global.opencv_highgui.waitKey;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

public class TestDotToSvg {
  private static final Logger log = LoggerFactory.getLogger(TestDotToSvg.class);

  @Test
  public void testSimpleDotToSvg() throws IOException, InterruptedException {
    String dot = "digraph { a -> b; b -> c }";

    String svg = Graphviz.dotToSvg(dot);

    BufferedImage expected = ImageIO.read(Objects.requireNonNull(this.getClass().getResourceAsStream("expected.svg")));
    BufferedImage actual = ImageIO.read(new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8)));

    Assertions.assertEquals(expected.getHeight(), actual.getHeight(), "Image heights differ");
    Assertions.assertEquals(expected.getWidth(), actual.getWidth(), "Image widths differ");

    Loader.load(opencv_java.class);

    OpenCVFrameConverter.ToMat openCVFrameConverter = new OpenCVFrameConverter.ToMat();
    Java2DFrameConverter java2DFrameConverter = new Java2DFrameConverter();
    Mat matExpected = openCVFrameConverter.convert(java2DFrameConverter.convert(expected));
    imshow("expected", matExpected);

    Mat grayExpected = new Mat(matExpected.size(), CV_8UC1);
    cvtColor(matExpected, grayExpected, CV_BGR2GRAY);
    threshold(grayExpected, grayExpected, 200, 255, CV_THRESH_BINARY);
    imshow("expected thresholded", grayExpected);

    Mat matActual = openCVFrameConverter.convert(java2DFrameConverter.convert(actual));
    imshow("actual", matActual);

    Mat grayActual = new Mat(matActual.size(), CV_8UC1);
    cvtColor(matActual, grayActual, CV_BGR2GRAY);
    threshold(grayActual, grayActual, 200, 255, CV_THRESH_BINARY);
    imshow("actual thresholded", grayActual);

    Mat result = new Mat(matActual.size(), CV_32FC1);
    matchTemplate(grayActual, grayExpected, result, TM_SQDIFF_NORMED);
    OpenCVFrameConverter.ToIplImage toIplImage = new OpenCVFrameConverter.ToIplImage();
    CvScalar sum = cvSum(toIplImage.convert(openCVFrameConverter.convert(result)));
    log.info("Sum of cross correlation = {}", sum);

    waitKey(0);

    Assertions.assertTrue(sum.val(0) <= 0.05);
  }
}
