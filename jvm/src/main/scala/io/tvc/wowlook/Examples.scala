package io.tvc.wowlook

import cats.instances.string._
import io.tvc.wowlook.Drawing.DrawingOptions
import java.awt.Color
import java.nio.file.{Files, Paths}

object Examples extends App {

  def writeFile(content: xml.Elem, name: String): Unit =
    Files.write(Paths.get(name), content.mkString.getBytes)

  val options = DrawingOptions[String](
    series = {
      case "Shiny people" => new Color(0xdd3333)
      case "Happy people" => new Color(0x33dd33)
    },
    axes = new Color(0x8e8e8e),
    grid = new Color(0xdedede),
    title = "Impact of REM on society",
    xSize = 600,
    ySize = 300
  )

  val barData = List(
    "1999" -> List(
      "Shiny people" -> 80,
      "Happy people" -> 40
    ),
    "2000" -> List(
      "Shiny people" -> 70,
      "Happy people" -> 80
    ),
    "2001" -> List(
      "Shiny people" -> 64,
      "Happy people" -> 82,
    ),
    "2002" -> List(
      "Shiny people" -> 42,
      "Happy people" -> 90,
    ),
    "2003" -> List(
      "Shiny people" -> 20,
      "Happy people" -> 110,
    ),
  )

  val barChart: BarGraph[String, String] =
    barData.foldLeft(BarGraph.empty[String, String]) { case (bg, (year, rem)) =>
      rem.foldLeft(bg) { case (g, (series, number)) => g.plot(year, series, BigDecimal(number)) }
    }

  val lineGraph: LineGraph[String, String] =
    barData.foldLeft(LineGraph.empty[String, String]) { case (bg, (year, rem)) =>
      rem.foldLeft(bg) { case (g, (series, number)) => g.plot(year, series, BigDecimal(number)) }
    }

  writeFile(barChart.render(options), "bar.svg")
  writeFile(lineGraph.render(options), "line.svg")
}
