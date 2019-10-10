package wiki.utils

import org.scalatest.{ FlatSpec, Matchers }

/**
 * Created by Bondarenko on Oct, 02, 2019 
 * 11:49 AM.
 * Project: Wikipedia
 */
class FormattingUtilsTest extends FlatSpec with Matchers with FormattingUtils {
  "FormattingUtils" should "format nanos in hh:mm:ss format" in {
	formatDurationInNanos(1000000000L * 3600) shouldBe "01:00:00"
	formatDurationInNanos(1000000000L * 3601) shouldBe "01:00:01"
	formatDurationInNanos(1000000000L * 3661) shouldBe "01:01:01"
	formatDurationInNanos(1000000000L * 60) shouldBe "00:01:00"
	formatDurationInNanos(0L) shouldBe "00:00:00"
  }
}
