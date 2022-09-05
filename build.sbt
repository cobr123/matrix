name := "matrix-rain"

version := "0.6"

scalaVersion := "3.1.3"

libraryDependencies += "org.jline" % "jline" % "3.21.0"

libraryDependencies += "dev.zio" %% "zio" % "2.0.1"

libraryDependencies += "com.github.scopt" %% "scopt" % "4.1.0"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio-test" % "2.0.1" % "test",
  "dev.zio" %% "zio-test-sbt" % "2.0.1" % "test",
)

testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
