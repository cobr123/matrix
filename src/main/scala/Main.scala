import cats.effect.{ExitCode, IO, IOApp, Resource}
import org.jline.terminal.{Terminal, TerminalBuilder}
import scopt.OParser
import upperbound.Limiter
import upperbound.syntax.rate.rateOps
import scala.concurrent.duration._


object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    OParser.parse(getParcer(), args, MatrixRainConfig()) match {
      case Some(config) =>
        runWithConfig(config)
      case _ =>
        // arguments are bad, error message will have been displayed
        IO(ExitCode.Error)
    }
  }

  def getParcer() = {
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

  private def runWithConfig(matrixRainConfig: MatrixRainConfig): IO[ExitCode] =
    makeTerminal().use { terminal =>
      makeMatrixRain(terminal, matrixRainConfig).use { matrixRain =>
        // 60FPS
        Limiter.start[IO](minInterval = 60 every 1.second).use { rateLimiter =>
          for {
            _ <- renderWithLimit(matrixRain, rateLimiter).foreverM
          } yield ExitCode.Success
        }
      }
    }

  private def renderWithLimit(matrixRain: MatrixRain, rateLimiter: Limiter[IO]): IO[Unit] =
    rateLimiter.submit(IO(matrixRain.renderFrame()))

  private def makeMatrixRain(terminal: Terminal, matrixRainConfig: MatrixRainConfig): Resource[IO, MatrixRain] =
    Resource.make(IO(MatrixRain(terminal, matrixRainConfig).start()))(mr => IO(mr.stop()))

  private def makeTerminal(): Resource[IO, Terminal] =
    Resource.fromAutoCloseable(
      IO(TerminalBuilder.builder()
        .jna(true)
        .system(true)
        .build())
    )

}