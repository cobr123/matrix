import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.google.common.util.concurrent.RateLimiter
import org.jline.terminal.{Terminal, TerminalBuilder}
import scopt.OParser


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
          if (List("ascii", "binary", "braille", "emoji", "katakana").contains(x.toLowerCase)) success
          else failure("Use rain characters from char-range {ascii, binary, braille, emoji, katakana}")
        )
        .text("{ascii, binary, braille, emoji, katakana}\nUse rain characters from char-range"),
    )
  }

  private def runWithConfig(matrixRainConfig: MatrixRainConfig): IO[ExitCode] =
    makeTerminal().use { terminal =>
      makeMatrixRain(terminal, matrixRainConfig).use { matrixRain =>
        for {
          rateLimiter <- IO(RateLimiter.create(60)) // 60FPS
          _ <- renderWithLimit(matrixRain, rateLimiter).foreverM
        } yield ExitCode.Success
      }
    }

  private def renderWithLimit(matrixRain: MatrixRain, rateLimiter: RateLimiter): IO[Unit] =
    for {
      _ <- IO(rateLimiter.acquire())
      _ <- IO(matrixRain.renderFrame())
    } yield ()

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