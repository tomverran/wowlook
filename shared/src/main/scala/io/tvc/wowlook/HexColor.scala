package io.tvc.wowlook

import java.awt.Color

/**
  * Tiny set of utilities for working with our old friend,
  * java.awt.Color
  */
object HexColor {

  private def hex(int: Int): String =
    int.toHexString.reverse.padTo(2, "0").reverse.mkString

  def hexString(color: Color): String =
    s"#${hex(color.getRed)}${hex(color.getGreen)}${hex(color.getBlue)}"
}
