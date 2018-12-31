package io.tvc.wowlook

import java.awt.Color

import cats.syntax.flatMap._

import scala.xml.{Elem, NodeBuffer}
import Snip._

import scala.collection.immutable.SortedSet
import scala.math.BigDecimal.RoundingMode

/**
  * Snipping aware SVG drawing functions
  * used throughout all the charts
  */
object Drawing {

  /**
    * How long the little sticks coming out of the axes are
    * I don't really know what their actual name is
    */
  private val graduationLength = 5

  /**
    * How much space to leave between the top of graduation labels
    * and the above graduations
    */
  private val labelPadding = 4

  case class DrawingOptions[A](
    series: A => Color,
    axes: Color,
    grid: Color,
    title: String,
    xSize: Int,
    ySize: Int
  )

  /**
    * Wraps the given NodeBuffer in basic SVG tags,
    * setting the width & height from the render options
    */
  def svg[A](opts: DrawingOptions[A])(elems: NodeBuffer): xml.Elem =
    <svg
      viewBox={s"0 0 ${opts.xSize} ${opts.ySize}"} xmlns="http://www.w3.org/2000/svg"
      font-family="sans-serif"
    >{elems}</svg>

  /**
    * Render a centered graph title,
    * applies a bit of padding first
    */
  def title(title: String): Snip[Elem] =
    pad(5, 0) >> current.map { box =>
      <text
        text-anchor="middle"
        x={box.centreX.toString}
        y={box.endY.toString}
      >{title}</text>
    }

  /**
    * Assuming a numeric Y axis, render graduations between 0 and the maximum value
    * with the given precision (i.e. a precision of 10 means 10 distinct points rendered)
    */
  def yLabels(maxValue: BigDecimal, precision: Int): Snip[NodeBuffer] =
    current.flatMap { remainder =>
      val yHeight = remainder.height / precision
      (0 to precision).foldLeft(pure(new NodeBuffer)) { case (nb, y) =>
          for {
            buffer <- nb
            box <- chopBottom(yHeight)
          } yield buffer &+
            <line
              x1={(box.endX - graduationLength).toString}
              x2={box.endX.toString}
              y1={box.endY.toString}
              y2={box.endY.toString}
              stroke="black"
            />
            <text
              text-anchor="end"
              dominant-baseline="central"
              x={(box.endX - graduationLength - labelPadding).toString}
              y={box.endY.toString}
            >{((maxValue / precision) * y).setScale(2, RoundingMode.HALF_UP)}
          </text>
      }.map { _ &+
        <line
          x1={remainder.endX.toString}
          x2={remainder.endX.toString}
          y1={remainder.startY.toString}
          y2={remainder.endY.toString}
          stroke="black"
        />
      }
    }

  /**
    * Render a labelled graph X-Axis
    * into the given snipped bounding box
    */
  def xLabels[X: Ordering, S: Ordering, V](data: DataTable[X, S, V]): Snip[NodeBuffer] =
    current.flatMap { box =>
      val xWidth = box.width / data.values.size
      data.values.keys.foldLeft(pure(new NodeBuffer)) { case (nb, x) =>
        for {
          buffer <- nb
          box <- chopLeft(xWidth)
        } yield buffer &+
          <text
            text-anchor="middle"
            dominant-baseline="hanging"
            x={box.centreX.toString}
            y={(box.startY + graduationLength + labelPadding).toString}
          >{s"$x"}
          </text> &+
          <line
            x1={box.centreX.toString}
            x2={box.centreX.toString}
            y1={box.startY.toString}
            y2={(box.startY + graduationLength).toString}
            stroke="black"
          />
      }.map { _ &+
        <line
          x1={box.startX.toString}
          x2={box.endX.toString}
          y1={box.startY.toString}
          y2={box.startY.toString}
          stroke="black"
        />
      }
    }

  /**
    * Create a key explaining which series correspond to which colours on the graph
    * Designed to be displayed horizontally beneath the x-axis
    */
  def key[X: Ordering, S: Ordering, V](data: DataTable[X, S, V], opts: DrawingOptions[S]): Snip[NodeBuffer] =
    current.flatMap { box =>

      val series = data.values.map(_._2.keySet).foldLeft(SortedSet.empty[S]) { case (s, s1) => s ++ s1 }
      val xWidth = box.width / series.size
      val radius = 6

      series.foldLeft(Snip.pure(new NodeBuffer)) { case (nb, s) =>
          for {
            buffer <- nb
            current <- chopLeft(xWidth)
          } yield buffer &+
            <circle
              r={radius.toString}
              fill={HexColor.hexString(opts.series(s))}
              cx={(current.startX + radius).toString}
              cy={current.centreY.toString}
            /> &+
            <text
              text-anchor="start"
              dominant-baseline="central"
              x={(current.startX + (radius * 2) + labelPadding).toString}
              y={current.centreY.toString}
            >{s.toString}</text>
      }
    }
}
