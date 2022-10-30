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
    nativeImageOptions ++= Seq(
      "--no-fallback",
      "--install-exit-handlers",
      "-H:IncludeResources=.*",
      s"-H:ReflectionConfigurationFiles=${(nativeImageAgentOutputDir.value / "reflect-config.json").absolutePath}",
    ),
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
  val projectRoot = baseDirectory.value
  val testExitCode = Process(Seq(testBinaryFile.absolutePath), cwd = Some(projectRoot)).!
  if (testExitCode != 0) {
    throw new Exception(s"Native image tests failed:\ncmd = ${testBinaryFile.absolutePath}\ncwd = ${projectRoot}")
  }
}

lazy val nativeImageRunTestAgent =
  taskKey[Unit]("Run tests, tracking all usages of dynamic features of an execution with `native-image-agent`.")

nativeImageRunTestAgent := {
  val _ = nativeImageCommand.value
  val graalHome = nativeImageGraalHome.value.toFile

  val agentConfig =
    if (nativeImageAgentMerge.value)
      "config-merge-dir"
    else
      "config-output-dir"
  val agentOption =
    s"-agentlib:native-image-agent=$agentConfig=${nativeImageAgentOutputDir.value}"

  (Test / assembly).value
  val assembledFile: String = (Test / assembly / assemblyOutputPath).value.getAbsolutePath

  val command = mutable.ListBuffer.empty[String]
  command += (graalHome / "bin" / "java").absolutePath
  command += agentOption
  command += "-jar"
  command += assembledFile

  val projectRoot = baseDirectory.value
  val exitCode = Process(command, cwd = Some(projectRoot)).!
  if (exitCode != 0) {
    throw new Exception(s"Native image build failed:\n ${command}")
  }
}
