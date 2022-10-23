import scala.sys.process.Process
import sbt.Keys._
import sbt._
import scala.collection.mutable

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
    nativeImageOptions ++= Seq("--no-fallback", "-H:IncludeResources=.*", "--install-exit-handlers"),
    nativeImageVersion := "22.1.0" // It should be at least version 21.0.0
  )
  .settings(inConfig(Test)(baseAssemblySettings): _*)

// Set up configuration for building a test assembly
Test / assembly / assemblyJarName := s"${name.value}-test-${version.value}.jar"
Test / assembly / assemblyMergeStrategy := (assembly / assemblyMergeStrategy).value
Test / assembly / assemblyOption := (assembly / assemblyOption).value
Test / assembly / assemblyShadeRules := (assembly / assemblyShadeRules).value

lazy val nativeImagePackageTest =
  taskKey[File]("Build a standalone executable with tests using GraalVM Native Image")

nativeImagePackageTest := {
  (Test / assembly).value

  val assembledFile: String = (Test / assembly / assemblyOutputPath).value.getAbsolutePath

  val testBinaryName = s"${name.value}-${version.value}-with-tests-native"
  val command = mutable.ListBuffer.empty[String]
  command ++= nativeImageCommand.value
  command += "-jar"
  command += assembledFile
  command ++= nativeImageOptions.value
  command += testBinaryName

  val cwd = (NativeImage / target).value
  cwd.mkdirs()

  val exitCode = Process(command, cwd = Some(cwd)).!
  if (exitCode != 0) {
    throw new Exception(s"Native image build failed:\n ${command}")
  } else {
    cwd / testBinaryName
  }
}

lazy val nativeImageRunTest =
  taskKey[Unit]("Build a standalone executable with tests using GraalVM Native Image and run it")

nativeImageRunTest := {
  val testBinaryFile = nativeImagePackageTest.value
  val testExitCode = Process(Seq(testBinaryFile.absolutePath), cwd = Some(testBinaryFile.getParentFile)).!
  if (testExitCode != 0) {
    throw new Exception(s"Native image tests failed:\n ${testBinaryFile.absolutePath} \ncwd = ${testBinaryFile.getParentFile}")
  }
}
