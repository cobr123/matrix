import zio._
import zio.test._
import zio.test.Assertion._
import zio.test.TestAspect.timeout

object MatrixRainConfigSpec extends ZIOSpecDefault {

  def spec = suite("MatrixRainConfigSpec")(
    test("--help") {
      Main.main(Array("--help"))
      assertTrue(true)
    }
  ) @@ timeout(5.seconds)

}
