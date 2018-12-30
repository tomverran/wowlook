package io.tvc.wowlook

import cats.instances.string._
import io.tvc.wowlook.Drawing.DrawingOptions
import java.awt.Color
import java.nio.file.{Files, Paths}

object Examples extends App {

  def writeFile(content: xml.Elem, name: String): Unit =
    Files.write(Paths.get(name), content.mkString.getBytes)

  val data = List(
    "Toms Cafe" -> List(
      500,
      600,
      700,
      702,
      708,
      900
    ),
    "Healthy House" -> List(
      100,
      150,
      180,
      190,
      210,
      350,
      380
    )
  )

  val options = DrawingOptions[String](
    series = {
      case "Toms Cafe"      | "Shiny people" => Color.RED
      case "Healthy House"  | "Happy people" => Color.GREEN
    },
    axes = Color.GRAY,
    grid = Color.GRAY,
    title = "Calorie Distribution",
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

  val graph: DistributionGraph[String] =
    data.foldLeft(DistributionGraph.empty[String]) { case (dg, (cafe, calories)) =>
      calories.foldLeft(dg) { case (g, number) => DistributionGraph.plot(cafe, number)(g) }
    }

  val barChart: BarGraph[String, String] =
    barData.foldLeft(BarGraph.empty[String, String]) { case (bg, (year, rem)) =>
      rem.foldLeft(bg) { case (g, (series, number)) => g.plot(year, series, BigDecimal(number)) }
    }

  writeFile(DistributionGraph.render(options)(graph), "distribution.svg")
  writeFile(barChart.render(options.copy(title = "Impact of REM on society")), "bar.svg")
}
