package com.example

import com.example.Ansi.color
import enumeratum._

sealed abstract class AnsiColor(num: Int) extends EnumEntry {
  def getTermCode: String = {
    color(num)
  }
}

object AnsiColor extends Enum[AnsiColor] {
  val values = findValues

  case object fgBlack extends AnsiColor(30)
  case object fgRed extends AnsiColor(31)
  case object fgGreen extends AnsiColor(32)
  case object fgYellow extends AnsiColor(33)
  case object fgBlue extends AnsiColor(34)
  case object fgMagenta extends AnsiColor(35)
  case object fgCyan extends AnsiColor(36)
  case object fgWhite extends AnsiColor(37)

  case object bgBlack extends AnsiColor(40)
  case object bgRed extends AnsiColor(41)
  case object bgGreen extends AnsiColor(42)
  case object bgYellow extends AnsiColor(43)
  case object bgBlue extends AnsiColor(44)
  case object bgMagenta extends AnsiColor(45)
  case object bgCyan extends AnsiColor(46)
  case object bgWhite extends AnsiColor(47)
}
