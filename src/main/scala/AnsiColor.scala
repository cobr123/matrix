import Ansi.{color, ctlEsc}

object AnsiColor {
  def fgRgb(r: Int, g: Int, b: Int): String = s"${ctlEsc}38;2;$r;$g;${b}m"

  def bgRgb(r: Int, g: Int, b: Int): String = s"${ctlEsc}48;2;$r;$g;${b}m"

  val fgBlack: String = color(30)
  val fgRed: String = color(31)
  val fgGreen: String = color(32)
  val fgYellow: String = color(33)
  val fgBlue: String = color(34)
  val fgMagenta: String = color(35)
  val fgCyan: String = color(36)
  val fgWhite: String = color(37)
  val bgBlack: String = color(40)
  val bgRed: String = color(41)
  val bgGreen: String = color(42)
  val bgYellow: String = color(43)
  val bgBlue: String = color(44)
  val bgMagenta: String = color(45)
  val bgCyan: String = color(46)
  val bgWhite: String = color(47)
}
