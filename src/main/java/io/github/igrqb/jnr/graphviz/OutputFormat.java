package io.github.igrqb.jnr.graphviz;

/**
 * Graphviz export output formats<br/>
 * <a href="https://graphviz.org/docs/outputs/">Output Formats | Graphviz</a>
 */
public enum OutputFormat {
  BMP("BMP", "bmp", "Windows Bitmap"),
  CGImage("CGImage", "cgimage", "Apple Core Graphics"),
  DOT_CANON("DOT", "canon", "Graphviz Language"),
  DOT("DOT", "dot", "Graphviz Language"),
  DOT_GV("DOT", "gv", "Graphviz Language"),
  DOT_XDOT("DOT", "xdot", "Graphviz Language"),
  DOT_XDOT_1_2("DOT", "xdot1.2", "Graphviz Language"),
  DOT_XDOT_1_4("DOT", "xdot1.4", "Graphviz Language"),
  EPS("EPS", "eps", "Encapsulated PostScript"),
  EXR("EXR", "exr", "OpenEXR"),
  FIG("FIG", "fig", "Xfig"),
  GD("GD", "gd", "LibGD"),
  GD2("GD2", "gd2", "LibGD"),
  GIF("GIF", "gif", "Graphics Interchange Format"),
  GTK("GTK", "gtk", "Formerly GTK+ / GIMP ToolKit"),
  ICO("ICO", "ico", "Windows Icon"),
  Imagemap("Imagemap", "imap", "Image Map: Server-side and client-side"),
  Imagemap_IMAP_NP("Imagemap", "imap_np", "Image Map: Server-side and client-side"),
  Imagemap_ISMAP("Imagemap", "ismap", "Image Map: Server-side and client-side"),
  Imagemap_CMAP("Imagemap", "cmap", "Image Map: Server-side and client-side"),
  Imagemap_CMAPX("Imagemap", "cmapx", "Image Map: Server-side and client-side"),
  Imagemap_CMAPX_NP("Imagemap", "cmapx_np", "Image Map: Server-side and client-side"),
  JPG("JPEG", "jpg", "Joint Photographic Experts Group"),
  JPEG("JPEG", "jpeg", "Joint Photographic Experts Group"),
  JPE("JPEG", "jpe", "Joint Photographic Experts Group"),
  JPEG_2000("JPEG 2000", "jp2", "JPEG 2000"),
  JSON("JSON", "json", "JavaScript Object Notation"),
  JSON0("JSON", "json0", "JavaScript Object Notation"),
  JSON_DOT("JSON", "dot_json", "JavaScript Object Notation"),
  JSON_XDOT("JSON", "xdot_json", "JavaScript Object Notation"),
  PDF("PDF", "pdf", "Portable Document Format"),
  PIC("PIC", "pic","Brian Kernighan's Diagram Language" ),
  PICT("PICT", "pct", "Apple PICT"),
  Plain("Plain Text", "plain", "Simple, line-based language"),
  Plain_EXT("Plain Text", "plain-ext", "Simple, line-based language"),
  PNG("PNG", "png", "Portable Network Graphics"),
  POV("POV-Ray", "pov", "Persistence of Vision Raytracer (prototype)"),
  PS("PS", "ps", "Adobe PostScript"),
  PS_PDF("PS/PDF", "ps2",  	"Adobe PostScript for Portable Document Format"),
  PSD("PSD", "psd", "Photoshop"),
  SGI("SGI", "sgi", "Silicon Graphics Image"),
  SVG("SVG", "svg", "Scalable Vector Graphics"),
  SVGZ("SVG", "svgz", "Scalable Vector Graphics"),
  TGA("TGA", "tga", "Truevision TARGA"),
  TIF("TIFF", "tif", "Tag Image File Format"),
  TIFF("TIFF", "tiff", "Tag Image File Format"),
  Tk("Tk", "tk", "Tcl/Tk"),
  VML("VML", "vml", "Vector Markup Language"),
  VMLZ("VML", "vmlz", "Vector Markup Language"),
  VRML("VRML", "vrml", "Virtual Reality Modeling Language"),
  WBMP("WBMP", "wbmp", "Wireless Bitmap"),
  WebP("WebP", "webp", "WebP"),
  X11("X11", "xlib", "X11 Window"),
  X11_XLIB("X11", "x11", "X11 Window");



  OutputFormat(String format, String commandLineParameter, String description) {
    this.format = format;
    this.commandLineParameter = commandLineParameter;
    this.description = description;
  }

  private final String format;
  private final String commandLineParameter;
  private final String description;

  public String getFormat() {
    return format;
  }

  public String getCommandLineParameter() {
    return commandLineParameter;
  }

  public String getDescription() {
    return description;
  }
}
