import zio._
import scopt.{DefaultOEffectSetup, OEffectSetup, OParser}

import java.util.concurrent.atomic.AtomicReference

case class MatrixRainConfig(direction: String = "v", color: String = "green", charRange: String = "ascii")

object MatrixRainConfig {

  def live: ZLayer[ZIOAppArgs, ExitCode, MatrixRainConfig] = ZLayer.fromZIO(getConfig)

  def getConfig: ZIO[ZIOAppArgs, ExitCode, MatrixRainConfig] =
    for {
      args <- ZIO.service[ZIOAppArgs]
      terminated = new AtomicReference(false)
      osetup = new DefaultOEffectSetup with OEffectSetup {
        override def terminate(exitState: Either[String, Unit]): Unit = terminated.set(true)
      }
      conf <- OParser.parse(getParser, args.getArgs.toList, MatrixRainConfig(), osetup) match {
        case Some(config) if !terminated.get =>
          ZIO.succeed(config)
        case _ =>
          // arguments are bad, error message will have been displayed
          ZIO.fail(ExitCode.failure)
      }
    } yield conf

  private def getParser: OParser[Unit, MatrixRainConfig] = {
    val builder = OParser.builder[MatrixRainConfig]
    import builder._
    OParser.sequence(
      programName("matrix-rain"),
      head("The famous Matrix rain effect of falling green characters as a cli command"),
      help("help").text("Show this help message and exit"),
      opt[String]('d', "direction")
        .action((x, c) => c.copy(direction = x))
        .text("{h, v}\nChange direction of rain. h=horizontal, v=vertical"),
      opt[String]('c', "color")
        .action((x, c) => c.copy(color = x))
        .validate(x =>
          if (List("green", "red", "blue", "yellow", "magenta", "cyan", "white").contains(x.toLowerCase)) success
          else failure("Use rain color from range {green, red, blue, yellow, magenta, cyan, white}")
        )
        .text("{green, red, blue, yellow, magenta, cyan, white}\nRain color. NOTE: droplet start is always white"),
      opt[String]('k', "char-range")
        .action((x, c) => c.copy(charRange = x))
        .validate(x =>
          if (List("ascii", "binary", "braille", "emoji", "katakana", "lil-guys").contains(x.toLowerCase)) success
          else failure("Use rain characters from char-range {ascii, binary, braille, emoji, katakana}")
        )
        .text("{ascii, binary, braille, emoji, katakana}\nUse rain characters from char-range"),
    )
  }
}