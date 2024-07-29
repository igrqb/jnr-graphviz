import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.CvScalar;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_java;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_highgui.imshow;
import static org.bytedeco.opencv.global.opencv_highgui.waitKey;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_imgproc.TM_SQDIFF_NORMED;

public class TestUtils {
  private static final Logger log = LoggerFactory.getLogger(TestUtils.class);

  private static final OpenCVFrameConverter.ToMat openCVFrameConverter = new OpenCVFrameConverter.ToMat();
  private static final Java2DFrameConverter java2DFrameConverter = new Java2DFrameConverter();
  private static final OpenCVFrameConverter.ToIplImage toIplImage = new OpenCVFrameConverter.ToIplImage();

  /**
   * Compares two buffered images using cross-correlation. The matching algorithm uses OpenCV's matchTemplate() function
   * with TM_SQDIFF argument. A result of zero indicates and exact match, while differences cause the overall sum of
   * squared differences to grow.
   * <br/>
   * @see <a href="https://docs.opencv.org/3.4/de/da9/tutorial_template_matching.html">OpenCV: Template Matching</a>
   * <br/>
   * @param expected expected image as BufferedImage
   * @param actual actual image as BufferedImage
   * @param tolerance minimum squared difference acceptable
   * @param interactive switch to true to see intermediate stages of image transformations - thresholding, etc.
   * @return true if the sum of squares of differences is less than the tolerance.
   */
  public static boolean compareImages(BufferedImage expected, BufferedImage actual, double tolerance, boolean interactive) {
    Loader.load(opencv_java.class);

    Mat matExpected = openCVFrameConverter.convert(java2DFrameConverter.convert(expected));

    if (interactive) {
      imshow("expected", matExpected);
    }

    Mat grayExpected = new Mat(matExpected.size(), CV_8UC1);
    cvtColor(matExpected, grayExpected, CV_BGR2GRAY);
    threshold(grayExpected, grayExpected, 200, 255, CV_THRESH_BINARY);

    if (interactive) {
      imshow("expected thresholded", grayExpected);
    }

    Mat matActual = openCVFrameConverter.convert(java2DFrameConverter.convert(actual));

    if (interactive) {
      imshow("actual", matActual);
    }

    Mat grayActual = new Mat(matActual.size(), CV_8UC1);
    cvtColor(matActual, grayActual, CV_BGR2GRAY);
    threshold(grayActual, grayActual, 200, 255, CV_THRESH_BINARY);

    if (interactive) {
      imshow("actual thresholded", grayActual);
    }

    Mat result = new Mat(matActual.size(), CV_32FC1);
    matchTemplate(grayActual, grayExpected, result, TM_SQDIFF_NORMED);
    CvScalar sum = cvSum(toIplImage.convert(openCVFrameConverter.convert(result)));

    log.info("Sum of cross correlation = {}", sum);

    if (interactive) {
      waitKey(0);
    }

    return sum.val(0) <= tolerance;
  }


  /**
   * Converts the input pdf stream to a buffered image. For multipage pdfs the first page is processed.
   *
   * @param in input pdf stream
   * @param dpi resolution of the conversion in DPI
   * @return buffered image of first page of pdf
   * @throws IOException if there is any problem reading the input stream
   */
  public static BufferedImage pdfToImage(InputStream in, int dpi) throws IOException {
    PDDocument document = org.apache.pdfbox.Loader.loadPDF(in.readAllBytes());
    PDFRenderer pdfRenderer = new PDFRenderer(document);
    return pdfRenderer.renderImageWithDPI(0, dpi, ImageType.RGB);
  }
}
