package io.tvc.wowlook

import cats.Monoid
import cats.instances.list._
import cats.instances.option._
import cats.syntax.foldable._
import cats.syntax.functor._
import cats.syntax.monoid._

import scala.collection.immutable.SortedMap
import scala.language.higherKinds

/**
  * A table of data - x-axis values are of type X
  * and for each of those values you'll have a series -> y value mapping
  */
case class DataTable[X, S, V](
  values: SortedMap[X, SortedMap[S, V]]
)

object DataTable {

  def empty[X: Ordering, S: Ordering, V]: DataTable[X, S, V] =
    DataTable(SortedMap.empty[X, SortedMap[S, V]])

  /**
    * Most of the functions in here hopefully won't be used
    * and I'll delete them when I've got a few more chart types
    */
  implicit class DataTableOps[X: Ordering, S: Ordering, V](dt: DataTable[X, S, V]) {

    /**
      * The main operation to be applied to this data table,
      * folds all the elements down into a B given an initial B and a function
      * that given an X Coordinate, a series, a value and the accumulated result performs some operation
      */
    def fold[B](z: B)(f: X => S => V => B => B): B =
      dt.values.foldLeft(z) { case (b, (x, ss)) => ss.foldLeft(b) { case (bb, (s, v)) => f(x)(s)(v)(bb) }}

    /**
      * Implementation of fold for types which have a Monoid instance available
      */
    def foldM[B](f: X => S => V => B)(implicit B: Monoid[B]): B =
      fold(B.empty)(x => s => v => b => b |+| f(x)(s)(v))

    /**
      * Convenience method to fold while accumulating the number of X values enumerated over
      * This is actually kind of tricky you see
      */
    def foldWithIndex[B](z: B)(f: (X, Int) => S => V => B => B): B =
      fold[(B, (Int, Option[X]))](z -> (0 -> None)) { x => s => v => {
        case (b, (i, prev)) if !prev.contains(x) => f(x, i + 1)(s)(v)(b) -> (i + 1, Some(x))
        case (b, (i, prev)) => f(x, i)(s)(v)(b) -> (i, prev)
      }}._1


    def foldX[B](f: (S, V) => B)(implicit B: Monoid[B]): SortedMap[X, B] =
      dt.values.mapValues(_.foldLeft(B.empty) { case (b, (s, v)) => b |+| f(s, v) })

    /**
      * Swap round the series and X-Axis in this Data Table
      * so the X axis becomes the series and vice versa
      */
    def sequence: DataTable[S, X, V] =
      DataTable(
        fold(SortedMap.empty[S, SortedMap[X, V]]) { x => s => v => map =>
          map.updated(s, map.getOrElse(s, SortedMap.empty[X, V]).updated(x, v))
        }
      )

    /**
      * Add a value to this DataTable
      */
    def replace(xAxis: X, series: S, value: V): DataTable[X, S, V] =
      DataTable(dt.values.updated(xAxis, dt.values.getOrElse(xAxis, SortedMap.empty[S, V]).updated(series, value)))

    /**
      * Given a monoid for V add an element to this DataTable
      * but combine it with any existing element at the same X and series
      */
    def add(x: X, series: S, value: V)(implicit V: Monoid[V]): DataTable[X, S, V] = {
      val newValue = dt.values.getOrElse(x, SortedMap.empty[S, V]).getOrElse(series, V.empty) |+| value
      replace(x, series, newValue)
    }

    /**
      * Given a list of X-Coordinate values,
      * add them to the data table with empty series
      */
    def padX(axes: List[X]): DataTable[X, S, V] =
      DataTable(
        axes.foldLeft(dt.values) { case (values, x) =>
          values.updated(x, values.getOrElse(x, SortedMap.empty[S, V]))
        }
      )

    /**
      * How many x-coordinates are there in the table?
      */
    def countXValues: Int =
      dt.values.keys.size

    def countSeries: Int =
      dt.values.foldLeft(0) { case (ac, sm) => Math.max(ac, sm._2.keys.size) }

    def max(implicit ordering: Ordering[V], monoid: Monoid[V]): V =
      fold(monoid.empty)(_ => _ => (ordering.max _).curried)

    /**
      * Sum all the points within the given series
      */
    def sum(series: S)(implicit m: Monoid[V]): V =
      dt.values.toList.foldMap[V] { case (_, v) => v.get(series).combineAll }
  }
}
