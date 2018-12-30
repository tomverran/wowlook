package io.tvc

import cats.data.State
import cats.instances.sortedMap._
import cats.syntax.flatMap._
import cats.syntax.functor._

package object wowlook {

  /**
    * A bounding box within which to draw something
    */
  case class BoundingBox(
    startX: Int,
    startY: Int,
    width: Int,
    height: Int
  ) {
    def centreX: Int = startX + (width / 2)
    def centreY: Int = startY + (height / 2)
    def endY: Int = startY + height
    def endX: Int = startX + width
  }

  /**
    * A snip represents a bounding box in the process of being carved up.
    * The remaining area to carve out is the state and each slice yields a new sub-box
    */
  type Snip[A] = State[BoundingBox, A]

  object Snip {

    /**
      * This just saves me admitting to the world
      * that all this Snip stuff is really just a State
      */
    def pure[A](a: A): Snip[A] =
      State.pure(a)

    /**
      * Given a snip that slices off a bounding box,
      * provide a snip that uses that box but then return the remainder
      */
    def use[B](a: Snip[BoundingBox], b: Snip[B]): Snip[B] =
      for {
        boundingBox <- a
        remainder <- Snip.current
        result <- Snip(boundingBox) >> b
        _ <- Snip(remainder)
      } yield result

    /**
      * Lift a bounding box into a snip
      */
    def apply(bb: BoundingBox): Snip[Unit] =
      State.set(bb)

    /**
      * Apply padding to the area we're currently snipping,
      * subtracting the given amounts evenly from the width & height
      */
    def pad(vertical: Int, horizontal: Int): Snip[Unit] =
      (chopBottom(vertical) >> chopTop(vertical) >> chopRight(horizontal) >> chopLeft(horizontal)).as(())

    def chopTop(y: Int): Snip[BoundingBox] =
      State { box =>
        (
          box.copy(startY = box.startY + y, height = box.height - y),
          BoundingBox(box.startX, box.startY, box.width, y)
        )
      }

    def chopBottom(y: Int): Snip[BoundingBox] =
      State { box =>
        (
          box.copy(startY = box.startY, height = box.height - y),
          BoundingBox(box.startX, box.endY - y, box.width, y)
        )
      }

    def chopLeft(x: Int): Snip[BoundingBox] =
      State { box =>
        (
          box.copy(startX = box.startX + x, width = box.width - x),
          BoundingBox(box.startX, box.startY, x, box.height)
        )
      }

    def chopRight(x: Int): Snip[BoundingBox] =
      State { box =>
        (
          box.copy(startX = box.startX, width = box.width - x),
          BoundingBox(box.endX - x, box.startY, x, box.height)
        )
      }

    def current: Snip[BoundingBox] =
      State.get
  }
}
