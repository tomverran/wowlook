package io.tvc.wowlook

import io.tvc.wowlook.Colour._
import org.scalacheck.Prop._
import org.scalacheck.{Arbitrary, Gen, _}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.Checkers

import scala.language.implicitConversions

class ColourTest extends AnyWordSpec with Checkers with Matchers {

  val colour: Gen[Colour] =
    Arbitrary.arbInt.arbitrary.map(i => Colour(Math.abs(i % 16777215)))

  val colourBidirectional: Prop =
    forAll(colour) { colour =>
      val converted = Colour(Integer.parseInt(s"${hexString(colour).drop(1)}", 16))
      (converted.r == colour.r: Prop) :| "Red channel preserved"  &&
      (converted.g == colour.g: Prop) :| "Blue channel preserved" &&
      (converted.b == colour.b: Prop) :| "Green channel preserved"
    }

  "Hex colour converter" should {

    "Split out integers into channels" in {
      Colour(0xffffff) shouldBe Colour(255.toByte, 255.toByte, 255.toByte)
      Colour(0x00ffffff) shouldBe Colour(255.toByte, 255.toByte, 255.toByte)
      Colour(0x000000) shouldBe Colour(0.toByte, 0.toByte, 0.toByte)
      Colour(0x808080) shouldBe Colour(128.toByte, 128.toByte, 128.toByte)
    }

    "Keep colour information for arbitrary colours" in {
      check(colourBidirectional)
    }
  }
}
