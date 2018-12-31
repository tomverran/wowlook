package io.tvc.wowlook
import java.awt.Color

import cats.instances.bigDecimal._
import cats.syntax.flatMap._
import io.tvc.wowlook.Drawing._
import io.tvc.wowlook.Snip._

import scala.collection.immutable.SortedMap
import scala.xml.{Elem, NodeBuffer}
import HexColor._

case class BarGraph[X, S](
  data: DataTable[X, S, BigDecimal]
)

object BarGraph {

  def empty[X: Ordering, S: Ordering] =
    BarGraph(data = DataTable.empty[X, S, BigDecimal])

  implicit class BarGraphOps[X: Ordering, S: Ordering](graph: BarGraph[X, S]) {

    /**
      * Plot a point into this bar graph with the given X & series values
      */
    def plot(x: X, s: S, amount: BigDecimal): BarGraph[X, S] =
      graph.copy(data = graph.data.add(x, s, amount))

    /**
      * Draw a single bar of the given height + colour,
      * taking up the entire area within the snip
      */
    private def drawBar(color: Color, height: BigDecimal): Snip[Elem] =
      current.map { box =>
        <rect
          x={box.startX.toString}
          y={(box.endY - height).toString}
          width={box.width.toString}
          height={height.toString}
          fill={hexString(color)}>
        </rect>
      }

    /**
      * Draw bars for a particular series
      * taking up the entire area within the snip
      */
    private def drawSeriesBars(
      series: SortedMap[S, BigDecimal],
      opts: DrawingOptions[S],
      maxValue: BigDecimal
    ): Snip[NodeBuffer] =
      current.flatMap { box =>
        val seriesWidth = box.width / graph.data.series.size
        series.foldLeft(pure(new NodeBuffer)) { case (nb, (s, bd)) =>
          for {
            buffer <- nb
            height = (bd / maxValue) * box.height
            bar <- use(chopLeft(seriesWidth), drawBar(opts.series(s), height))
          } yield buffer &+ bar
        }
      }

    /**
      * Go through all the data within the graph and draw bars for each X point,
      * this'll also take up the entire available snip area but will add some padding between bars
      */
    private def drawAllBars(opts: DrawingOptions[S]): Snip[NodeBuffer] =
      Snip.current.flatMap { box =>

        val xFactor = box.width / graph.data.countXValues
        val yFactor: BigDecimal = graph.data.max

        graph.data.values.foldLeft(Snip.pure(new NodeBuffer)) { case (canvas, (_, s)) =>
          for {
            buffer <- canvas
            bars <- use(chopLeft(xFactor), pad(0, 5) >> drawSeriesBars(s, opts, yFactor))
          } yield buffer &+ bars
        }
      }

    /**
      * Create an SVG representation of the current graph
      * This can then be saved to a file or embedded in a web page
      */
    def render(opts: DrawingOptions[S]): Elem =
      entireGraph(graph.data, opts, drawAllBars(opts))
  }
}
