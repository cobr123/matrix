import Ansi.color

enum AnsiColor(num: Int) {

  case fgBlack extends AnsiColor(30)
  case fgRed extends AnsiColor(31)
  case fgGreen extends AnsiColor(32)
  case fgYellow extends AnsiColor(33)
  case fgBlue extends AnsiColor(34)
  case fgMagenta extends AnsiColor(35)
  case fgCyan extends AnsiColor(36)
  case fgWhite extends AnsiColor(37)

  case bgBlack extends AnsiColor(40)
  case bgRed extends AnsiColor(41)
  case bgGreen extends AnsiColor(42)
  case bgYellow extends AnsiColor(43)
  case bgBlue extends AnsiColor(44)
  case bgMagenta extends AnsiColor(45)
  case bgCyan extends AnsiColor(46)
  case bgWhite extends AnsiColor(47)

  def getTermCode: String = {
    color(num)
  }
}
