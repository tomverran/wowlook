package io.tvc.wowlook

case class Colour(r: Byte, g: Byte, b: Byte)

object Colour {

  /**
   * Allows you to construct colours with hexadecimal values,
   * i.e. 0xffffff, which is more like what is used in CSS
   * There is probably a cleverer way of doing this...
   */
  def apply(value: Int): Colour =
    Colour(
      (value >>> 16 & 0x0000ff).toByte,
      (value >>> 8  & 0x0000ff).toByte,
      (value        & 0x0000ff).toByte,
    )

  /**
    * The takeRight(2) here is because ScalaJS doesn't format bytes well
    * owing to JS storing things as 32 bit integers internally
    */
  private def hex(byte: Byte): String =
    f"$byte%x".takeRight(2).reverse.padTo(2, "0").reverse.mkString

  def hexString(color: Colour): String =
    s"#${hex(color.r)}${hex(color.g)}${hex(color.b)}"
}
