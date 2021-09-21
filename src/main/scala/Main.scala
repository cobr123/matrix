import cats.effect.{IO, IOApp, Resource}
import com.google.common.util.concurrent.RateLimiter
import org.jline.terminal.{Terminal, TerminalBuilder}


object Main extends IOApp.Simple {

  override def run: IO[Unit] = {
    makeTerminal().use { terminal =>
      makeMatrixRain(terminal).use { matrixRain =>
        for {
          rateLimiter <- IO(RateLimiter.create(60)) // 60FPS
          _ <- renderWithLimit(matrixRain, rateLimiter).foreverM
        } yield ()
      }
    }
  }

  private def renderWithLimit(matrixRain: MatrixRain, rateLimiter: RateLimiter): IO[Unit] =
    for {
      _ <- IO(rateLimiter.acquire())
      _ <- IO(matrixRain.renderFrame())
    } yield ()

  private def makeMatrixRain(terminal: Terminal): Resource[IO, MatrixRain] =
    Resource.make(IO(MatrixRain(terminal).start()))(mr => IO(mr.stop()))

  private def makeTerminal(): Resource[IO, Terminal] =
    Resource.fromAutoCloseable(
      IO(TerminalBuilder.builder()
        .jna(true)
        .system(true)
        .build())
    )

}