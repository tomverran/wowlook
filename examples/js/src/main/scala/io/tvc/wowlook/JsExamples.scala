package io.tvc.wowlook
import org.scalajs.dom._
import cats.instances.string._
import Examples._


object JsExamples {

  def main(args: Array[String]): Unit =
    document.body.innerHTML =
      lineGraph.render(options).toString +
      barChart.render(options).toString
}
