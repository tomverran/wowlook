package io.tvc.wowlook

import cats.instances.string._
import org.scalatest.{Matchers, WordSpec}
import cats.instances.int._
import org.scalacheck.{Arbitrary, Gen, Prop}
import org.scalatest.prop.Checkers
import org.scalacheck.Prop._
import cats.instances.list._
import cats.syntax.foldable._
import DataTable._

import scala.collection.immutable.SortedMap

class DataTableTest extends WordSpec with Matchers with Checkers {

  val emptyTable: DataTable[String, String, Int] =
    DataTable.empty[String, String, Int]

  val singleItemTable: Gen[DataTable[String, String, Int]] =
    for {
      x <- Arbitrary.arbString.arbitrary
      s <- Arbitrary.arbString.arbitrary
      v <- Arbitrary.arbInt.arbitrary
    } yield emptyTable.add(x, s, v)

  val multiItemTable: Gen[DataTable[String, String, Int]] =
    Gen.listOf(singleItemTable).map(_.combineAll)

  "DataTable empty function" should {

    "Create a constant empty instance" in {
      emptyTable shouldBe DataTable(SortedMap.empty[String, SortedMap[String, Int]])
    }
  }

  "DataTable replace function" should {

    "Add points to the table when they don't exist already" in {
      val expected = DataTable(SortedMap("x1" -> SortedMap("s1" -> 1, "s2" -> 1)))
      emptyTable.replace("x1", "s1", 1).replace("x1", "s2", 1) shouldBe expected
    }

    "Replace points with identical x and series coordinates" in {
      val expected = DataTable(SortedMap("x1" -> SortedMap("s1" -> 5)))
      emptyTable.replace("x1", "s1", 1).replace("x1", "s1", 5) shouldBe expected
    }
  }

  "DataTable add function" should {

    "Behave identically to replace when dealing with non overlapping points" in {
      val expected = emptyTable.replace("x1", "s1", 1).replace("x1", "s2", 1)
      emptyTable.add("x1", "s1", 1).add("x1", "s2", 1) shouldBe expected
    }

    "Combine points at the same coordinate with a semigroup instance" in {
      val expected = DataTable(SortedMap("x1" -> SortedMap("s1" -> 6)))
      emptyTable.add("x1", "s1", 1).add("x1", "s1", 5) shouldBe expected
    }
  }

  "DataTable collectSeries function" should {

    val emptyFunction: PartialFunction[String, String] =
      PartialFunction.empty

    val collectSeries: Prop = forAll(multiItemTable) { table =>
      (table.collectSeries(emptyFunction) == emptyTable: Prop)  :| "collectSeries.empty" &&
      (table.collectSeries { case s => s } == table: Prop)      :| "collectSeries.identity"
    }

    "Return empty tables and identical tables for empty + identity functions respectively" in {
      check(collectSeries)
    }

    "Combine the values when multiple series are mapped down into one" in {
      val result = emptyTable.add("x1", "s1", 1).add("x1", "s2", 1).collectSeries { case _ => "s3" }
      result shouldBe emptyTable.add("x1", "s3", 2)
    }
  }
}
