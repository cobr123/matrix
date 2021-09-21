object Ansi {
  val ctlEsc = "\u001b["

  val reset = s"${ctlEsc}c"
  val clearScreen = s"${ctlEsc}2J"
  val cursorHome = s"${ctlEsc}H"

  def cursorPos(row: Long, col: Long): String = s"$ctlEsc$row;${col}H"

  val cursorVisible = s"$ctlEsc?25h"
  val cursorInvisible = s"$ctlEsc?25l"
  val useAltBuffer = s"$ctlEsc?47h"
  val useNormalBuffer = s"$ctlEsc?47l"
  val underline = s"${ctlEsc}4m"
  val off = s"${ctlEsc}0m"
  val bold = s"${ctlEsc}1m"

  def color(c: Int): String = s"$ctlEsc$c;1m"

  def fgRgb(r: Int, g: Int, b: Int): String = s"${ctlEsc}38;2;$r;$g;${b}m"

  def bgRgb(r: Int, g: Int, b: Int): String = s"${ctlEsc}48;2;$r;$g;${b}m"
}
