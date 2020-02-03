package wiki

import org.scalatest.{FlatSpec, Matchers}

/**
 * Created by Bondarenko on Nov, 29, 2019
 * 12:35.
 * Project: Wikipedia
 */
class LinksTest extends FlatSpec with Matchers with WikiTextExtractor {
  "WikiExtractor" should "extract wiki links" in {
    val s =
      """
	   |More recently, the British [[anarcho-pacifist|pacifist]] Alex Comfort gained notoriety during the 
	   |[[sexual revolution]] for writing the bestseller sex manual ''[[The Joy of Sex]]''. 
	   |The issue of free love has a dedicated treatment in the work of French anarcho-[[hedonist]] philosopher [[Michel Onfray]]
	   | in such works as ...
        |""".stripMargin

    collectWikiLinks(s) shouldBe List("anarcho-pacifist",
                                      "sexual revolution",
                                      "the joy of sex",
                                      "hedonist",
                                      "michel onfray")
  }

  it should "exclude file links" in {
    val s =
      "[[File:Drexel-newseal.png|150px]]"
    collectWikiLinks(s) shouldBe List("File:Drexel-newseal.png")
  }

}
