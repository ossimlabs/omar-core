package omar.core

import java.awt.image.BufferedImage
import java.awt.Graphics2D
import java.awt.Color
import javax.imageio.ImageIO
import groovy.xml.StreamingMarkupBuilder
import java.text.AttributedString
import java.text.AttributedCharacterIterator
import java.awt.font.FontRenderContext
import java.awt.font.LineBreakMeasurer
import java.awt.font.TextLayout
import java.awt.Graphics
import omar.core.HttpStatus
import java.awt.image.IndexColorModel
import java.awt.image.ColorModel
import java.awt.image.WritableRaster

class OgcExceptionUtil
{

  static transactional = false

  static def getBackgroundColor(def color, def alpha)
  {
    def result = new Color(255, 255, 255, alpha)
    if ( color )
    {
      if ( color.size() == 8 )
      {
        // skip 0x
        result = new Color(Integer.decode("0x" + color[2] + color[3]),
                Integer.decode("0x" + color[4] + color[5]),
                Integer.decode("0x" + color[6] + color[7]))
      }
    }

    return result
  }

  public static BufferedImage convertRGBAToIndexed(BufferedImage src)
  {
    BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);
    Graphics g = dest.getGraphics();
    g.setColor(new Color(231, 20, 189));
    g.fillRect(0, 0, dest.getWidth(), dest.getHeight()); //fill with a hideous color and make it transparent
    dest = makeTransparent(dest, 0, 0);
    dest.createGraphics().drawImage(src, 0, 0, null);
    return dest;
  }
  public static BufferedImage makeTransparent(BufferedImage image, int x, int y) {
    ColorModel cm = image.getColorModel();
    if (!(cm instanceof IndexColorModel))
      return image; //sorry...
    IndexColorModel icm = (IndexColorModel) cm;
    WritableRaster raster = image.getRaster();
    int pixel = raster.getSample(x, y, 0); //pixel is offset in ICM's palette
    int size = icm.getMapSize();
    byte[] reds = new byte[size];
    byte[] greens = new byte[size];
    byte[] blues = new byte[size];
    icm.getReds(reds);
    icm.getGreens(greens);
    icm.getBlues(blues);
    IndexColorModel icm2 = new IndexColorModel(8, size, reds, greens, blues, pixel);
    return new BufferedImage(icm2, raster, image.isAlphaPremultiplied(), null);
  }

  /**
   *
   * @param message is the actual text string message to write to an image
   * @param ogcParams additional ogc params that can be used to determine the output
   *                  image type, width, and height
   * @return BufferedImage
   */
  static def createErrorImage(def message, def ogcParams)
  {
    def imageWidth = ogcParams.width ? ogcParams.width as Integer : 512
    def imageHeight = ogcParams.height ? ogcParams.height as Integer : 256
    def image = null
    def transparent = ogcParams?.transparent ?: false
    def format = ogcParams.format ? ogcParams.format.toLowerCase() : "image/gif"

    if ( imageWidth > 2048 ) imageWidth = 2048
    if ( imageHeight > 2048 ) imageHeight = 2048
    // check forced transparency
    if ( transparent)
    { 
      if(format?.contains("jpeg") )
      {
        format = "image/gif"
      }
    }
    else  //determine if we are transparent
    {
      switch ( format )
      {
      case "image/png":
      case "image/gif":
        transparent = true
        break
      default:
        transparent = false
        break
      }
    }
    if ( !transparent )
    {
      image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB)
    }
    else
    {
      image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB)
    }

    Graphics2D g2d = image.createGraphics()
    def background = getBackgroundColor(ogcParams."bgcolor", transparent ? 0 : 255)
    g2d.setColor(background)
    g2d.setBackground(background)
    g2d.fillRect(0, 0, imageWidth, imageHeight)

    g2d.setColor(Color.BLACK)
    // Now let's implement a text wrapper if the error string is longer than the width of
    // the image it will wrap in the output
    //
    if ( message )
    {
      def x = 0
      def y = 0
      AttributedString attrStr = new AttributedString(message);
      // Get iterator for string:
      AttributedCharacterIterator characterIterator = attrStr.getIterator();
      // Get font context from graphics:
      FontRenderContext fontRenderContext = g2d.getFontRenderContext();
      // Create measurer:
      LineBreakMeasurer measurer = new LineBreakMeasurer(characterIterator,
              fontRenderContext);
      def done = false
      while ( !done && (measurer.getPosition() < characterIterator.getEndIndex()) )
      {
        TextLayout textLayout = measurer.nextLayout(imageWidth);
        y += textLayout.getAscent(); //Have tried changing y to x
        if ( y < imageHeight )
        {
          textLayout.draw(g2d, (int) x, (int) y);

          y += textLayout.getDescent() + textLayout.getLeading();
        }
        else  // we have exceeded the height of the image
        {
          done = true
        }
      }
    }
    if ( (format == "image/gif") && transparent )
    {
      image = convertRGBAToIndexed(image)
    }

    image
  }
  /**
   * Exception types handled:
   *    application/vnd.ogc.se_inimage   or any sub string that contains inimag
   *    application/vnd.ogc.se_blank or any sub string containing blank
   *    application/vnd.ogc.se_xml, text/xml or xml 
   *    text/plain
   *
   * @param ogcParams Is a param map.  For this method it expects a key of "exceptions"
   *
   * @return currently returns a text string and can have value "blank", image","xml", or "text"
   */
  static def determineOutputType(def ogcParams)
  {
    def exception = (ogcParams.exceptions ?: "text/plain").toLowerCase()
    def result

      if(exception.contains("xml"))
      {
          result = "xml"
      }
      else if(exception.contains("blank")){
          result = "blank"
      }
      else if(exception.contains("text"))
      {
          result = "text"
      }
      else if(exception.contains("image"))
      {
          result = "image"
      }
     result
  }
  static Byte[] imageToByteArray(def image, String outputType)
  {
      Byte[] result
      def ostream = new ByteArrayOutputStream()
      try{
        ImageIO.write(image, outputType, ostream)
        result = ostream.toByteArray()

      }
      catch(e)
      {
        result = "${e}".bytes
      }

      result
  }

  static HashMap formatOgcExceptionForResponse(def params, def message)
  {
    def result = [buffer: null,
                  contentType: null]

    def outputType = determineOutputType(params)
    switch ( outputType )
    {
      case "text":
        result.contentType = "text/plain"
        result.buffer = message?message.bytes:"".bytes
        break
      case "xml":
        def xmlbuilder = new StreamingMarkupBuilder()
        xmlbuilder.encoding = "UTF-8"
        def xmlNode = {
          mkp.xmlDeclaration()
          ServiceExceptionReport {
            ServiceException(message)
          }
        }
        result.contentType = "text/xml"
        result.buffer = xmlbuilder.bind(xmlNode).toString().bytes
        break;
      case "blank":
      case "image":
        String contentType = "image/gif"
        switch ( params.format )
        {
        case "image/jpeg":
        case "image/png":
        case "image/tiff":
          contentType = params.format
          break
        default:
          contentType = "image/gif"
        }
        result.contentType = contentType
        def image
        if ( outputType == "blank" )
        {
          image = createErrorImage("", params)
        }
        else
        {
          image = createErrorImage(message, params)
        }
        def ostream = new ByteArrayOutputStream()
        ImageIO.write(image, contentType.split("/")[-1], ostream)
        result.buffer = ostream.toByteArray()
        break
      default:
        result.contentType = "text/plain"
        result.buffer = message?message.bytes:"".bytes
        break
    }
    result
  }
  static def createErrorPairs(def cmd)
  {
    def result = [[:]]
    cmd.errors?.each { err ->
      def field = "${err.fieldError.arguments[0]}"
      def code = err.getFieldError( field )?.code
      result << [field: field, code: code]
    }
    result
  }

  static String createErrorString(def cmd)
  {
    String errorString = ""
    def errorPairs = createErrorPairs(cmd)
    errorPairs.each { pair ->
      if ( pair.code )
      {
        errorString += ( "${pair.field}: ${pair.code}\n" )
      }
    }

    errorString
  }

  static HashMap formatWcsException(def cmd)
  {
    def result = [buffer: null,
                  contentType: null]
    if ( cmd.hasErrors() )
    {
      result = formatOgcExceptionForResponse(cmd, "WCS server Error: " + "${createErrorString(cmd)}")
    }

    result
  }

  static HashMap formatWmsException(def cmd)
  {
    def result = [buffer: null,
                  contentType: null]
    if ( cmd.hasErrors() )
    {
      result = formatOgcExceptionForResponse(cmd, "WMS server Error: " + "${createErrorString(cmd)}")
    }

    result
  }
}
