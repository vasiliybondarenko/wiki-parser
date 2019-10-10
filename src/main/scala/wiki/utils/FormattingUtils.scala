package wiki.utils

/**
 * Created by Bondarenko on Oct, 02, 2019 
 * 11:48 AM.
 * Project: Wikipedia
 */
trait FormattingUtils {
  private def fmt(n: Long) = if(n < 10) s"0$n" else s"$n"

  def formatDurationInNanos(nanos: Long): String = {
    val sec = nanos / 1000000000L
    s"${fmt(sec / 3600)}:${fmt(sec / 60 % 60)}:${fmt(sec % 60)}"
  }
}
