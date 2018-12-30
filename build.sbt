name := "wowlook"

organization in ThisBuild := "io.tvc"

version in ThisBuild := "0.1-SNAPSHOT"

scalaVersion in ThisBuild := "2.12.8"

resolvers += Resolver.sonatypeRepo("releases")

scalacOptions ++= Seq("-Ypartial-unification", "-feature")

import sbtcrossproject.CrossPlugin.autoImport.crossProject
lazy val wowlook = (crossProject(JSPlatform, JVMPlatform) in file("."))
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

lazy val root = project.in(file("."))
  .aggregate(wowlook.js, wowlook.jvm)
  .settings(publish := {}, publishLocal := {})
