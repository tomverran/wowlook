package io.tvc.wowlook

import cats.instances.string._
import java.nio.file.{Files, Paths}
import Examples._

object JvmExamples extends App {

  def writeFile(content: xml.Elem, name: String): Unit =
    Files.write(Paths.get(name), content.mkString.getBytes)

  writeFile(barChart.render(options), "bar.svg")
  writeFile(lineGraph.render(options), "line.svg")
}
