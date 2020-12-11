package io.tvc.wowlook

import cats.{Order, Show}
import cats.instances.bigDecimal._
import Drawing._
import Snip._
import io.tvc.wowlook.Colour.hexString

import scala.collection.SortedMap
import scala.xml.{Elem, NodeBuffer}

final case class LineGraph[X, S](
  data: DataTable[X, S, BigDecimal]
)

object LineGraph {

  def empty[S: Order, X: Order]: LineGraph[X, S] =
    LineGraph(DataTable.empty[X, S, BigDecimal])

  implicit class LineGraphOps[X: Order, S: Order: Show](graph: LineGraph[X, S]) {

    private val zero: BigDecimal =
      BigDecimal(0)

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
        graph.data.series.toList.map { series =>
          <marker id={seriesSlug(series)} markerHeight="4" markerWidth="4" refX="2" refY="2">
            <circle cx="2" cy="2" r="2" stroke="none" fill={hexString(opts.series(series))}/>
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
        val emptyMap = SortedMap.empty[S, String](Order[S].toOrdering)
        val xWidth = box.width / graph.data.values.size
        val allSeries = graph.data.series
        val maxValue = graph.data.max

        graph.data.values.foldLeft(Snip.pure(emptyMap)) { case (map, (_, series)) =>
          for {
            accumulator <- map
            area <- chopLeft(xWidth)
          } yield {
            allSeries.foldLeft(accumulator) { case (acc, s) =>
              val c = s" ${area.centreX},${box.endY - ((series.getOrElse(s, zero) / maxValue) * box.height)}"
              acc + (s -> (acc.getOrElse(s, "") + c))
            }
          }
        }.map { lines =>
          lines.foldLeft(new NodeBuffer) { case (nb, (s, points)) =>
            nb &+ <polyline
              points={points}
              stroke-width="2"
              marker-start={s"url(#${seriesSlug(s)})"}
              marker-mid={s"url(#${seriesSlug(s)})"}
              marker-end={s"url(#${seriesSlug(s)})"}
              stroke={hexString(opts.series(s))}
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
      entireGraph(graph.data, opts, drawAllLines(opts).map(_ &+ markers(opts)))
  }
}
