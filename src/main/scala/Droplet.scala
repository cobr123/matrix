final class Droplet(var alive: Long,
                    var curCol: Int,
                    var curRow: Int,
                    var height: Int,
                    var speed: Int,
                    var chars: Array[String]
                   ) {

  def getCharOrEmpty(idx: Int): String = {
    if (idx < 0 || idx >= chars.length) {
      ""
    } else {
      chars(idx)
    }
  }

  def reset(d: Droplet): Unit = {
    alive = d.alive
    curCol = d.curCol
    curRow = d.curRow
    height = d.height
    speed = d.speed
    chars = d.chars
  }

}
