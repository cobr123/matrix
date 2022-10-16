name := "matrix-rain"

version := "0.7"

scalaVersion := "2.13.8"

val zioVersion = "2.0.2"

libraryDependencies += "org.jline" % "jline" % "3.21.0"

libraryDependencies += "dev.zio" %% "zio" % zioVersion

libraryDependencies += "com.github.scopt" %% "scopt" % "4.1.0"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio-test" % zioVersion % "test",
  "dev.zio" %% "zio-test-sbt" % zioVersion % "test",
  "com.beachape" %% "enumeratum" % "1.7.0",
)

testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))

lazy val root = (project in file("."))
  .enablePlugins(NativeImagePlugin)
  .settings(
    Compile / mainClass := Some("com.example.Main"),
    nativeImageOptions += "--no-fallback",
    nativeImageVersion := "22.1.0" // It should be at least version 21.0.0
  )
