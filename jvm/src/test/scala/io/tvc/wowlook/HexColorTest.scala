package io.tvc.wowlook

import java.awt.Color
import java.lang.Math.abs

import io.tvc.wowlook.HexColor.hexString
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.WordSpec
import org.scalatest.prop.Checkers
import org.scalacheck.Prop._
import org.scalacheck._

import scala.language.implicitConversions

class HexColorTest extends WordSpec with Checkers {

  val colour: Gen[Color] = for {
    red <- Arbitrary.arbByte.arbitrary
    green <- Arbitrary.arbByte.arbitrary
    blue <- Arbitrary.arbByte.arbitrary
  } yield new Color(abs(red.toInt), abs(green.toInt), abs(blue.toInt))

  val colourBidirectional: Prop =
    forAll(colour) { colour =>
      val converted = Color.decode(hexString(colour))
      (converted.getRed == colour.getRed: Prop)     :| "Red channel preserved"  &&
      (converted.getBlue == colour.getBlue: Prop)   :| "Blue channel preserved" &&
      (converted.getGreen == colour.getGreen: Prop) :| "Green channel preserved"
    }

  "Hex colour converter" should {
    "Keep colour information for arbitrary colours" in {
      check(colourBidirectional)
    }
  }
}
