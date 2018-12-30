name in ThisBuild := "wow-look"

version in ThisBuild := "0.1"

scalaVersion in ThisBuild := "2.12.8"

resolvers in ThisBuild += Resolver.sonatypeRepo("releases")

scalacOptions in ThisBuild ++= Seq("-Ypartial-unification", "-feature")

import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}
lazy val project = (crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Full) in file("."))
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "1.5.0",
      "org.typelevel" %%% "cats-effect" % "1.1.0",
      "org.scalatest" %%% "scalatest" % "3.0.0" % Test,
      "org.typelevel" %%% "cats-testkit" % "1.5.0" % Test,
      "org.scala-lang.modules" %%% "scala-xml" % "1.1.1",
      compilerPlugin("org.spire-math" % "kind-projector" % "0.9.8").cross(CrossVersion.binary),
    )
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-simple" % "1.7.25"
    )
  )
