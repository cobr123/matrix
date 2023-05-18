import sbt.Keys._
import sbt._

name := "matrix-rain"

version := "0.7"

scalaVersion := "2.13.8"

val zioVersion = "2.0.3"

libraryDependencies += "org.jline" % "jline" % "3.21.0"

libraryDependencies += "dev.zio" %% "zio" % zioVersion

libraryDependencies += "com.github.scopt" %% "scopt" % "4.1.0"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio-test" % zioVersion % "test",
  "dev.zio" %% "zio-test-sbt" % zioVersion % "test",
  "com.beachape" %% "enumeratum" % "1.7.0",
)


lazy val root = (project in file("."))
  .enablePlugins(NativeImagePlugin)
  .settings(
    Compile / mainClass := Some("com.example.Main"),
    nativeImageOptions ++= Seq(
      "--no-fallback",
      "--install-exit-handlers",
      "-H:IncludeResources=.*",
    ) ++ Option(nativeImageAgentOutputDir.value)
      .filter(_.exists())
      .map(file => s"-H:ReflectionConfigurationFiles=${(file / "reflect-config.json").absolutePath}")
      .toSeq,
    nativeImageVersion := "22.3.0" // It should be at least version 21.0.0
  )
