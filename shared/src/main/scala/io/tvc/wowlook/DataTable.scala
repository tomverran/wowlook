package io.tvc.wowlook

import cats.instances.sortedMap._
import cats.kernel.{CommutativeMonoid, Semigroup}
import cats.syntax.functor._
import cats.syntax.monoid._
import cats.{Monoid, Order}

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

  def empty[X: Order, S: Order, V: Semigroup]: DataTable[X, S, V] =
    monoid[X, S, V].empty

  /**
    * CommutativeMonoid instance for DataTable,
    * based on the standard instance for SortedMap which intellij struggles to find
    */
  implicit def monoid[X: Order, S: Order, V: Semigroup]: CommutativeMonoid[DataTable[X, S, V]] = {
    val underlying = catsStdMonoidForSortedMap[X, SortedMap[S, V]]
    new CommutativeMonoid[DataTable[X, S, V]] {
      def empty: DataTable[X, S, V] =
        DataTable(underlying.empty)
      def combine(x: DataTable[X, S, V], y: DataTable[X, S, V]): DataTable[X, S, V] =
        DataTable(underlying.combine(x.values, y.values))
    }
  }

  /**
    * Most of the functions in here hopefully won't be used
    * and I'll delete them when I've got a few more chart types
    */
  implicit class DataTableOps[X: Order, S: Order, V](dt: DataTable[X, S, V]) {

    implicit val xOrdering: Ordering[X] = Order[X].toOrdering
    implicit val sOrdering: Ordering[S] = Order[S].toOrdering

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
      * Given a partial function mapping series
      * produce a new data table containing a new series
      */
    def collectSeries[S2: Order](f: PartialFunction[S, S2])(implicit V: Semigroup[V]): DataTable[X, S2, V] =
      fold(DataTable.empty[X, S2, V])(x => s => v => t => if (f.isDefinedAt(s)) t.add(x, f(s), v) else t)

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
    def max(implicit ordering: Order[V], monoid: Monoid[V]): V =
      fold(monoid.empty)(_ => _ => (ordering.max _).curried)
  }
}
