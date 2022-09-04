import zio.*
import scopt.{DefaultOEffectSetup, OEffectSetup, OParser}

import java.io.File
import java.util.concurrent.atomic.AtomicReference
import javax.imageio.ImageIO
import java.awt.{Color, Graphics, Image}
import java.awt.image.BufferedImage
import java.util.concurrent.CopyOnWriteArrayList
import scala.collection.mutable

case class MatrixRainConfig(direction: String = "v",
                            color: String = "green",
                            charRange: String = "ascii",
                            maskPath: Option[String] = None,
                            mask: Option[Array[Array[Char]]] = None,
                            invertMask: Boolean = false,
                            offsetRow: Int = 0,
                            offsetCol: Int = 0,
                            fontRatio: Int = 2,
                            printMask: Boolean = false)

case object ShowHelpException extends Exception
case object PrintMaskWithoutPath extends Exception
case object PrintMaskPath extends Exception

final class MyOEffectSetup(consoleLogList: CopyOnWriteArrayList[String], consoleErrLogList: CopyOnWriteArrayList[String], terminated: AtomicReference[Boolean])
    extends OEffectSetup {
  override def displayToOut(msg: String): Unit = {
    consoleLogList.add(msg)
  }

  override def displayToErr(msg: String): Unit = {
    consoleErrLogList.add(msg)
  }

  override def reportError(msg: String): Unit = {
    displayToErr("Error: " + msg)
  }

  override def reportWarning(msg: String): Unit = {
    displayToErr("Warning: " + msg)
  }
  override def terminate(exitState: Either[String, Unit]): Unit = terminated.set(true)
}

object MatrixRainConfig {

  def live: ZLayer[ZIOAppArgs, Throwable, MatrixRainConfig] =
    ZLayer.fromZIO(getConfig)

  def getConfig: ZIO[ZIOAppArgs, Throwable, MatrixRainConfig] =
    for {
      args <- ZIO.service[ZIOAppArgs]
      terminated = new AtomicReference(false)
      consoleLogList = new CopyOnWriteArrayList[String]()
      consoleErrLogList = new CopyOnWriteArrayList[String]()
      osetup = new MyOEffectSetup(consoleLogList, consoleErrLogList, terminated)
      conf <- OParser.parse(getParser, args.getArgs.toList, MatrixRainConfig(), osetup) match {
        case Some(config) if !terminated.get => ZIO.succeed(config)
        // arguments are bad, error message will have been displayed
        case _ =>
          Console.printLine(consoleLogList.toArray.mkString("\n")) *>
            Console.printLineError(consoleErrLogList.toArray.mkString("\n")) *>
            ZIO.fail(ShowHelpException)
      }
      _ <- (Console.printLineError("Error: Missing option --mask-path\nTry --help for more information.") *> ZIO.fail(PrintMaskWithoutPath))
        .when(conf.printMask && conf.maskPath.isEmpty)
    } yield conf

  def updateConfigWithMask(matrixRainInit: MatrixRain): ZIO[Any, Throwable, MatrixRain] =
    for {
      config <- if (matrixRainInit.matrixRainConfig.maskPath.isDefined)
        MatrixRainConfig.fillMask(matrixRainInit.matrixRainConfig, matrixRainInit.terminal.getWidth, matrixRainInit.terminal.getHeight)
      else ZIO.succeed(matrixRainInit.matrixRainConfig)
      matrixRain = matrixRainInit.copy(matrixRainConfig = config)
    } yield matrixRain

  def fillMask(config: MatrixRainConfig, width: Int, height: Int): ZIO[Any, Throwable, MatrixRainConfig] = ZIO.scoped {
    for {
      bufferedImageInit <- ZIO.attempt(ImageIO.read(new File(config.maskPath.get)))
      newHeight <- ZIO.attempt(height.max(bufferedImageInit.getHeight / 8))
      newWidth <- ZIO.attempt(width.max(bufferedImageInit.getWidth / 4))
      image = bufferedImageInit.getScaledInstance(newWidth * config.fontRatio, newHeight , Image.SCALE_DEFAULT)
      bufferedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB)
      _ <- makeGraphics(bufferedImage).map(_.drawImage(image, 0, 0, null))
      mask <- ZIO.succeed(convert(bufferedImage))
      _ <- (printMask(config, mask) *> ZIO.fail(PrintMaskPath)).when(config.printMask)
    } yield config.copy(mask = Some(mask))
  }

  private def printMask(config: MatrixRainConfig, mask: Array[Array[Char]]) = {
    for {
      _ <- Console.print(Array.fill(config.offsetRow)("\n").mkString)
      _ <- Console.printLine(mask.map(chars => s"${Array.fill(config.offsetCol)(" ").mkString}${chars.mkString}").mkString("\n"))
    } yield ()
  }

  private def makeGraphics(bufferedImage: BufferedImage): ZIO[Scope, Throwable, Graphics] =
    ZIO.acquireRelease(
      ZIO.attempt(bufferedImage.getGraphics)
    )(t => ZIO.succeed(t.dispose()))

  private def convert(image: BufferedImage): Array[Array[Char]] = {
    val arr = new Array[Array[Char]](image.getHeight)
    for (y <- 0 until image.getHeight) {
      val line = new Array[Char](image.getWidth)
      for (x <- 0 until image.getWidth) {
        val pixelColor: Color = new Color(image.getRGB(x, y))
        val gValue: Double = pixelColor.getRed.toDouble * 0.3 + pixelColor.getBlue.toDouble * 0.59 + pixelColor.getGreen.toDouble * 0.11
        line(x) = getChar(gValue)
      }
      arr(y) = line
    }
    arr
  }

  private val chars = Array('@', '#', '8', '&', '^', '+', '*', '.', ' ')

  private def getChar(g: Double): Char = {
    if (g >= 240) chars(0)
    else if (g >= 210) chars(1)
    else if (g >= 190) chars(2)
    else if (g >= 170) chars(3)
    else if (g >= 120) chars(4)
    else if (g >= 110) chars(5)
    else if (g >= 80) chars(6)
    else if (g >= 60) chars(8)
    else chars(8)
  }

  private def getParser = {
    val builder = OParser.builder[MatrixRainConfig]
    import builder._
    OParser.sequence(
      programName("matrix-rain"),
      head("The famous Matrix rain effect of falling green characters as a cli command"),
      help('h', "help").text("Show this help message and exit"),
      opt[String]('d', "direction")
        .action((x, c) => c.copy(direction = x))
        .text("{h, v}\nChange direction of rain. h=horizontal, v=vertical"),
      opt[String]('c', "color")
        .action((x, c) => c.copy(color = x))
        .validate(x =>
          if (List("green", "red", "blue", "yellow", "magenta", "cyan", "white").contains(x.toLowerCase)) success
          else failure("Use rain color from range {green, red, blue, yellow, magenta, cyan, white}"))
        .text("{green, red, blue, yellow, magenta, cyan, white}\nRain color. NOTE: droplet start is always white"),
      opt[String]('k', "char-range")
        .action((x, c) => c.copy(charRange = x))
        .validate(x =>
          if (List("ascii", "binary", "braille", "emoji", "katakana", "lil-guys").contains(x.toLowerCase)) success
          else failure("Use rain characters from char-range {ascii, binary, braille, emoji, katakana}"))
        .text("{ascii, binary, braille, emoji, katakana}\nUse rain characters from char-range"),
      opt[String]('m', "mask-path")
        .action((x, c) => c.copy(maskPath = Some(x)))
        .validate(x =>
          if (new File(x).exists()) success
          else failure(s"File '$x' for mask-path not found"))
        .text("Use the specified image to build a mask for the raindrops."),
      opt[Unit]('i', "invert-mask")
        .action((_, c) => c.copy(invertMask = true))
        .text("Invert the mask specified with --mask-path."),
      opt[Int]("offset-row")
        .action((x, c) => c.copy(offsetRow = x))
        .text("Move the upper left corner of the mask down n rows."),
      opt[Int]("offset-col")
        .action((x, c) => c.copy(offsetCol = x))
        .text("Move the upper left corner of the mask right n columns."),
      opt[Int]("font-ratio")
        .action((x, c) => c.copy(fontRatio = x))
        .text("Ratio between character height over width in the terminal."),
      opt[Unit]("print-mask")
        .action((_, c) => c.copy(printMask = true))
        .text("Print mask and exit."),
    )
  }
}
