name := "wowlook"

organization in ThisBuild := "io.tvc"

resolvers += Resolver.sonatypeRepo("releases")

scalacOptions ++= Seq("-Ypartial-unification", "-feature")

import sbtcrossproject.CrossPlugin.autoImport.crossProject
lazy val wowlook = (crossProject(JSPlatform, JVMPlatform) in file("."))
  .enablePlugins(GitVersioning)
  .settings(
    git.useGitDescribe := true,
    git.gitTagToVersionNumber := (tag => Some(tag).filter(_.matches("[0-9]+\\..*"))),
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "1.5.0",
      "org.typelevel" %%% "cats-effect" % "1.1.0",
      "org.scala-lang.modules" %%% "scala-xml" % "1.1.1",
      compilerPlugin("org.spire-math" % "kind-projector" % "0.9.8").cross(CrossVersion.binary),
    )
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-simple" % "1.7.25",
      "org.scalacheck" %% "scalacheck" % "1.14.0" % Test,
      "org.typelevel" %%% "cats-testkit" % "1.5.0" % Test,
      "org.scalatest" %% "scalatest" % "3.0.5" % Test
    )
  )

lazy val examples = (crossProject(JSPlatform, JVMPlatform) in file("examples"))
  .settings(publish := {}, publishLocal := {})
  .dependsOn(wowlook)

lazy val root = project.in(file("."))
  .settings(publish := {}, publishLocal := {})
  .aggregate(wowlook.js, wowlook.jvm)
