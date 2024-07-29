package io.github.igrqb.jnr.graphviz;

/**
 * Graphviz export output formats<br/>
 * <a href="https://graphviz.org/docs/outputs/">Output Formats | Graphviz</a>
 */
public enum OutputFormat {
  /**
   * Windows Bitmap
   */
  BMP("BMP", "bmp", "Windows Bitmap"),
  /**
   * Apple Core Graphics
   */
  CGImage("CGImage", "cgimage", "Apple Core Graphics"),
  /**
   * Graphviz Language
   */
  DOT_CANON("DOT", "canon", "Graphviz Language"),
  /**
   * Graphviz Language
   */
  DOT("DOT", "dot", "Graphviz Language"),
  /**
   * Graphviz Language
   */
  DOT_GV("DOT", "gv", "Graphviz Language"),
  /**
   * Graphviz Language
   */
  DOT_XDOT("DOT", "xdot", "Graphviz Language"),
  /**
   * , "Graphviz Language
   */
  DOT_XDOT_1_2("DOT", "xdot1.2", "Graphviz Language"),
  /**
   * , "Graphviz Language
   */
  DOT_XDOT_1_4("DOT", "xdot1.4", "Graphviz Language"),
  /**
   * Encapsulated PostScript
   */
  EPS("EPS", "eps", "Encapsulated PostScript"),
  /**
   * OpenEXR
   */
  EXR("EXR", "exr", "OpenEXR"),
  /**
   * Xfig
   */
  FIG("FIG", "fig", "Xfig"),
  /**
   * LibGD
   */
  GD("GD", "gd", "LibGD"),
  /**
   * LibGD
   */
  GD2("GD2", "gd2", "LibGD"),
  /**
   * Graphics Interchange Format
   */
  GIF("GIF", "gif", "Graphics Interchange Format"),
  /**
   * Formerly GTK+ / GIMP ToolKit
   */
  GTK("GTK", "gtk", "Formerly GTK+ / GIMP ToolKit"),
  /**
   * Windows Icon
   */
  ICO("ICO", "ico", "Windows Icon"),
  /**
   * Image Map: Server-side and client-side
   */
  Imagemap("Imagemap", "imap", "Image Map: Server-side and client-side"),
  /**
   * Image Map: Server-side and client-side
   */
  Imagemap_IMAP_NP("Imagemap", "imap_np", "Image Map: Server-side and client-side"),
  /**
   * Image Map: Server-side and client-side
   */
  Imagemap_ISMAP("Imagemap", "ismap", "Image Map: Server-side and client-side"),
  /**
   * Image Map: Server-side and client-side
   */
  Imagemap_CMAP("Imagemap", "cmap", "Image Map: Server-side and client-side"),
  /**
   * Image Map: Server-side and client-side
   */
  Imagemap_CMAPX("Imagemap", "cmapx", "Image Map: Server-side and client-side"),
  /**
   * Image Map: Server-side and client-side
   */
  Imagemap_CMAPX_NP("Imagemap", "cmapx_np", "Image Map: Server-side and client-side"),
  /**
   * Joint Photographic Experts Group
   */
  JPG("JPEG", "jpg", "Joint Photographic Experts Group"),
  /**
   * Joint Photographic Experts Group
   */
  JPEG("JPEG", "jpeg", "Joint Photographic Experts Group"),
  /**
   * Joint Photographic Experts Group
   */
  JPE("JPEG", "jpe", "Joint Photographic Experts Group"),
  /**
   *  "JPEG 2000
   */
  JPEG_2000("JPEG 2000", "jp2", "JPEG 2000"),
  /**
   * JavaScript Object Notation
   */
  JSON("JSON", "json", "JavaScript Object Notation"),
  /**
   * JavaScript Object Notation
   */
  JSON0("JSON", "json0", "JavaScript Object Notation"),
  /**
   * JavaScript Object Notation
   */
  JSON_DOT("JSON", "dot_json", "JavaScript Object Notation"),
  /**
   * JavaScript Object Notation
   */
  JSON_XDOT("JSON", "xdot_json", "JavaScript Object Notation"),
  /**
   * Portable Document Format
   */
  PDF("PDF", "pdf", "Portable Document Format"),
  /**
   * Brian Kernighan's Diagram Language
   */
  PIC("PIC", "pic","Brian Kernighan's Diagram Language" ),
  /**
   * Apple PICT
   */
  PICT("PICT", "pct", "Apple PICT"),
  /**
   *  "Simple, line-based language
   */
  Plain("Plain Text", "plain", "Simple, line-based language"),
  /**
   * ", "Simple, line-based language
   */
  Plain_EXT("Plain Text", "plain-ext", "Simple, line-based language"),
  /**
   * Portable Network Graphics
   */
  PNG("PNG", "png", "Portable Network Graphics"),
  /**
   * , "Persistence of Vision Raytracer (prototype)
   */
  POV("POV-Ray", "pov", "Persistence of Vision Raytracer (prototype)"),
  /**
   * Adobe PostScript
   */
  PS("PS", "ps", "Adobe PostScript"),
  /**
   * ,  	"Adobe PostScript for Portable Document Format
   */
  PS_PDF("PS/PDF", "ps2",  	"Adobe PostScript for Portable Document Format"),
  /**
   * Photoshop
   */
  PSD("PSD", "psd", "Photoshop"),
  /**
   * Silicon Graphics Image
   */
  SGI("SGI", "sgi", "Silicon Graphics Image"),
  /**
   * Scalable Vector Graphics
   */
  SVG("SVG", "svg", "Scalable Vector Graphics"),
  /**
   * Scalable Vector Graphics
   */
  SVGZ("SVG", "svgz", "Scalable Vector Graphics"),
  /**
   * Truevision TARGA
   */
  TGA("TGA", "tga", "Truevision TARGA"),
  /**
   * Tag Image File Format
   */
  TIF("TIFF", "tif", "Tag Image File Format"),
  /**
   * Tag Image File Format
   */
  TIFF("TIFF", "tiff", "Tag Image File Format"),
  /**
   * Tcl/Tk
   */
  Tk("Tk", "tk", "Tcl/Tk"),
  /**
   * Vector Markup Language
   */
  VML("VML", "vml", "Vector Markup Language"),
  /**
   * Vector Markup Language
   */
  VMLZ("VML", "vmlz", "Vector Markup Language"),
  /**
   * Virtual Reality Modeling Language
   */
  VRML("VRML", "vrml", "Virtual Reality Modeling Language"),
  /**
   * Wireless Bitmap
   */
  WBMP("WBMP", "wbmp", "Wireless Bitmap"),
  /**
   * WebP
   */
  WebP("WebP", "webp", "WebP"),
  /**
   * X11 Window
   */
  X11("X11", "xlib", "X11 Window"),
  /**
   * X11 Window
   */
  X11_XLIB("X11", "x11", "X11 Window");



  OutputFormat(String format, String commandLineParameter, String description) {
    this.format = format;
    this.commandLineParameter = commandLineParameter;
    this.description = description;
  }

  private final String format;
  private final String commandLineParameter;
  private final String description;

  /**
   * Get the official format name
   * @return format name
   */
  public String getFormat() {
    return format;
  }

  /**
   * Get the command line parameter. This is the string Graphviz uses when producing the output
   * @return command line parameter
   */
  public String getCommandLineParameter() {
    return commandLineParameter;
  }

  /**
   * Description of the output format
   * @return description of format
   */
  public String getDescription() {
    return description;
  }
}
