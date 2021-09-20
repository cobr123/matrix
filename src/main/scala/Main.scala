import java.util.{Timer, TimerTask}

object Main {

  def main(args: Array[String]): Unit = {
    val matrixRain = MatrixRain()
    try {
      matrixRain.start()
      new Timer().scheduleAtFixedRate(new TimerTask() {
        @Override
        def run(): Unit = {
          matrixRain.renderFrame()
        }
      }, 0, 16) // 60FPS
      // infinite sleep
      Thread.currentThread().join()
    } finally {
      matrixRain.stop()
    }
  }
}