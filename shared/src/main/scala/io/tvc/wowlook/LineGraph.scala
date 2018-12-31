package io.tvc.wowlook
import cats.instances.bigDecimal._
import io.tvc.wowlook.Drawing._
import cats.syntax.flatMap._
import Snip._
import cats.Show

import scala.collection.SortedMap
import scala.xml.{Elem, NodeBuffer}

final case class LineGraph[X, S](
  data: DataTable[X, S, BigDecimal]
)

object LineGraph {

  def empty[S: Ordering, X: Ordering]: LineGraph[X, S] =
    LineGraph(DataTable.empty[X, S, BigDecimal])

  implicit class LineGraphOps[X: Ordering, S: Ordering: Show](graph: LineGraph[X, S]) {

    def plot(x: X, s: S, v: BigDecimal): LineGraph[X, S] =
      graph.copy(data = graph.data.add(x, s, v))

    /**
      * Create a lowercase hyphenated version of a series
      * for use in identifiers + CSS classes
      */
    private def seriesSlug(series: S): String =
      Show[S].show(series).toLowerCase.replaceAll("\\W+", "-")

    /**
      * Create a bunch of markers
      * which allow us to add blobs to polylines
      */
    private def markers(opts: DrawingOptions[S]): Elem =
      <defs>
      {
        graph.data.series.map { series =>
          <marker id={seriesSlug(series)} markerHeight="4" markerWidth="4" refX="2" refY="2">
            <circle cx="2" cy="2" r="2" stroke="none" fill={HexColor.hexString(opts.series(series))}/>
          </marker>
        }
      }
      </defs>

    /**
      * Turn the DataTable into a series of PolyLines to draw into the graph
      * We do this by creating a map from series -> points and then mapping the values into polylines
      */
    private def drawAllLines(opts: Drawing.DrawingOptions[S]): Snip[NodeBuffer] =
      Snip.current.flatMap { box =>

        // these operations are grim enough to not inline
        val xWidth = box.width / graph.data.values.size
        val allSeries = graph.data.series
        val maxValue = graph.data.max

        graph.data.values.foldLeft(Snip.pure(SortedMap.empty[S, String])) { case (map, (_, series)) =>
          for {
            accumulator <- map
            area <- chopLeft(xWidth)
          } yield {
            allSeries.foldLeft(accumulator) { case (acc, s) =>
              val coord = s" ${area.centreX},${(series.getOrElse(s, BigDecimal(0)) / maxValue) * box.height}"
              acc.updated(s, acc.getOrElse(s, "") + coord)
            }
          }
        }.map { lines =>
          lines.foldLeft(new NodeBuffer) { case (nb, (s, points)) =>
            nb &+ <polyline
              points={points}
              marker-start={s"url(#${seriesSlug(s)})"}
              marker-mid={s"url(#${seriesSlug(s)})"}
              marker-end={s"url(#${seriesSlug(s)})"}
              stroke={HexColor.hexString(opts.series(s))}
              stroke-linecap="round"
              fill="none"
            />
          }
        }
      }

    /**
      * Create an SVG representation of the current graph
      * This can then be saved to a file or embedded in a web page
      */
    def render(opts: DrawingOptions[S]): Elem =
      (
        for {
          _     <- pad(5, 50)
          title <- use(chopTop(20), title(opts.title))
          yAxis <- use(chopLeft(60), chopBottom(80) >> yLabels(graph.data.max, 10))
          key   <- use(chopBottom(60), key(graph.data, opts))
          xAxis <- use(chopBottom(20), xLabels(graph.data))
          data  <- drawAllLines(opts)
        } yield svg(opts)(yAxis &+ markers(opts) &+ data &+ title &+ key &+ xAxis)
      ).runA(BoundingBox(0, 0, opts.xSize, opts.ySize)).value
  }
}
