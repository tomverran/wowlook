package io.tvc.wowlook

import java.nio.file.{Files, Paths}

import cats.data.NonEmptyList
import cats.instances.int._
import cats.syntax.flatMap._
import cats.{Monoid, Show}
import io.tvc.wowlook.DataTable._
import io.tvc.wowlook.DistributionGraph.{Bin, Count}
import io.tvc.wowlook.Drawing._
import io.tvc.wowlook.Snip.{apply => _, _}
import HexColor._

import scala.xml.{Elem, NodeBuffer}

case class DistributionGraph[Series](
  data: DataTable[Series, Bin, Count],
  binCount: Int,
  binSize: Int
)

object DistributionGraph {

  type Bin = Int
  type Count = Int

  implicit val spaceString: Monoid[String] =
    new Monoid[String] {
      def empty: String = ""
      def combine(x: String, y: String): String = s"$x $y".stripPrefix(" ").stripSuffix(" ")
    }

  def empty[Series: Ordering]: DistributionGraph[Series] =
    DistributionGraph(DataTable.empty[Series, Bin, Count], 0, 200)

  private def generateBins(point: BigDecimal, binSize: Int): NonEmptyList[Bin] =
    NonEmptyList.fromListUnsafe((0 to (point / binSize).toInt).toList)

  /**
    * Add a point to this distribution graph,
    * will add 1 to the number of points recorded within the bin this data point lies within
    */
  def plot[S: Ordering](series: S, point: BigDecimal)(graph: DistributionGraph[S]): DistributionGraph[S] = {
    val bins = generateBins(point, graph.binSize)
    graph.copy(
      binCount = Math.max(graph.binCount, bins.length),
      data = generateBins(point, graph.binSize).foldLeft(graph.data) {
        case (data, bin) if ((bin + 1) * graph.binSize) > point => data.add(series, bin, 1)
        case (data, bin) => data.add(series, bin, 0)
      }
    )
  }

  def series[S: Show](series: S, data: String, opts: DrawingOptions[S], box: BoundingBox): Elem =
    <polyline
      class={s"series-${Show[S].show(series)}"}
      points={data + s" ${box.endX},${box.endY}"}
      fill={s"${hexString(opts.series(series))}00"}
      stroke={hexString(opts.series(series))}>
    </polyline>

  def render[S: Show: Ordering](opts: DrawingOptions[S])(graph: DistributionGraph[S]): xml.Elem =
    (
      for {
        _ <- pad(5, 50)
        title <- use(chopTop(20), title(opts.title))
        yLabels <- use(chopLeft(40), chopBottom(20) >> yLabels(graph.data.max, graph.data.max))
        xLabels <- use(chopBottom(20), xLabels(graph.data))
        data <- current
      } yield {

        val xFactor = data.width / graph.binCount
        val yFactor: BigDecimal = graph.data.max

        def calculateCoordinate(x: Int, count: Int): String =
          s"${data.startX + (x * xFactor)},${data.endY - ((count / yFactor) * data.height)}"

        val points = graph.data
          .foldX { case (bin, count) => calculateCoordinate(bin, count) }
          .foldLeft(new NodeBuffer) { case (nb, (s, v)) => nb &+ series(s, v, opts, data) }

        svg(opts)(yLabels &+ xLabels &+ points  &+ title)
      }
    ).runA(BoundingBox(0, 0, opts.xSize, opts.ySize)).value

}


