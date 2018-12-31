package io.tvc.wowlook
import org.scalatest.{Matchers, WordSpec}
import cats.instances.int._

import scala.collection.immutable.SortedMap

class DataTableTest extends WordSpec with Matchers {

  // used in a whole bunch of tests
  val emptyTable: DataTable[String, String, Int] = DataTable.empty[String, String, Int]

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

    "Combine points at the same coordinate with a monoid instance" in {
      val expected = DataTable(SortedMap("x1" -> SortedMap("s1" -> 6)))
      emptyTable.add("x1", "s1", 1).add("x1", "s1", 5) shouldBe expected
    }
  }
}
