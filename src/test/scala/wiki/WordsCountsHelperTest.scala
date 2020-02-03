package wiki

import org.scalatest.{FlatSpec, Matchers}

/**
 * Created by Bondarenko on Jan, 23, 2020
 * 16:27.
 * Project: Wikipedia
 */
class WordsCountsHelperTest extends FlatSpec with Matchers with WordsCountsHelper {

  it should "convert text to words" in {
    val text =
      """
		|Intersecting and overlapping between various schools of thought, certain topics of interest and internal disputes have proven perennial within anarchist theory.
		|""".stripMargin

    parseWords(text) should contain("intersecting")
    parseWords(text) should contain("certain")
  }

}
