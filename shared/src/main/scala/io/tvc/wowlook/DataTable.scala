package io.tvc.wowlook

import cats.Monoid
import cats.instances.list._
import cats.instances.option._
import cats.kernel.Semigroup
import cats.syntax.foldable._
import cats.syntax.functor._
import cats.syntax.monoid._

import scala.collection.immutable.{SortedMap, SortedSet}
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
      * Add a value to this DataTable
      */
    def replace(xAxis: X, series: S, value: V): DataTable[X, S, V] =
      DataTable(dt.values.updated(xAxis, dt.values.getOrElse(xAxis, SortedMap.empty[S, V]).updated(series, value)))

    /**
      * Given a semigroup for V add an element to this DataTable
      * but combine it with any existing element at the same X and series
      */
    def add(x: X, series: S, value: V)(implicit V: Semigroup[V]): DataTable[X, S, V] =
      (
        for {
          s <- dt.values.get(x)
          v <- s.get(series)
        } yield v |+| value
      ).fold(replace(x, series, value))(replace(x, series, _))

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

    /**
      * Obtain a set of every series plotted into this table
      * not every x-coordinate will necessarily have a point for every series
      */
    def series: SortedSet[S] =
      dt.values.foldLeft(SortedSet.empty[S]) { case (ac, sm) => ac ++ sm._2.keySet }

    /**
      * Find the maximum value in the table
      */
    def max(implicit ordering: Ordering[V], monoid: Monoid[V]): V =
      fold(monoid.empty)(_ => _ => (ordering.max _).curried)

    /**
      * Sum all the points within the given series
      */
    def sum(series: S)(implicit m: Monoid[V]): V =
      dt.values.toList.foldMap[V] { case (_, v) => v.get(series).combineAll }
  }
}
