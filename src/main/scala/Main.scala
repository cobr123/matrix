import org.jline.terminal.{Terminal, TerminalBuilder}
import zio._

object Main extends ZIOAppDefault {

  override def run =
    program
      .provideLayer(matrixRainLayer)
      .catchAll {
        case ShowHelpException    => ZIO.succeed(ExitCode.success)
        case PrintMaskWithoutPath => ZIO.succeed(ExitCode.success)
        case PrintMaskPath        => ZIO.succeed(ExitCode.success)
        case e: Throwable         => ZIO.fail(e.printStackTrace())
      }

  def program: ZIO[MatrixRain, Throwable, Unit] = {
    for {
      matrixRainInit <- ZIO.service[MatrixRain]
      matrixRain <- MatrixRainConfig.updateConfigWithMask(matrixRainInit)
      _ <- ZIO.attempt(matrixRain.start())
      // 60FPS
      _ <- ZIO
        .attempt(matrixRain.renderFrame())
        .repeat(Schedule.spaced((1000 / 60).millis))
        .catchAll(_ => program)
        .onInterrupt(ZIO.succeed(matrixRain.stop()))
    } yield ()
  }

  def configAndTerminalLayer: ZLayer[Scope with ZIOAppArgs, Any, Terminal with MatrixRainConfig] = ZLayer.fromZIO(makeTerminal) ++ MatrixRainConfig.live

  def matrixRainLayer: ZLayer[Scope with ZIOAppArgs, Any, MatrixRain] = configAndTerminalLayer >>> ZLayer.fromZIO(makeMatrixRain)

  private def makeTerminal: ZIO[Scope, Throwable, Terminal] =
    ZIO.acquireRelease(
      ZIO.attempt(
        TerminalBuilder
          .builder()
          .jna(true)
          .system(true)
          .build())
    )(t => ZIO.succeed(t.close()))

  private def makeMatrixRain: ZIO[Scope with Terminal with MatrixRainConfig, Throwable, MatrixRain] =
    for {
      terminal <- ZIO.service[Terminal]
      matrixRainConfig <- ZIO.service[MatrixRainConfig]
      matrixRain <- ZIO.acquireRelease(ZIO.attempt(MatrixRain(terminal, matrixRainConfig)))(mr => ZIO.succeed(mr.stop()))
    } yield matrixRain
}
