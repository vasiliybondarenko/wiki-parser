package wiki


import org.scalatest._
import org.scalatest.concurrent.{Signaler, ThreadSignaler, TimeLimitedTests, TimeLimits}
import org.scalatest.time.SpanSugar._
import wiki.Implicits._

import scala.io.Source

/**
  * Created by Bondarenko on 5/9/18.
  */
class WikiTests extends FlatSpec with Timed with Matchers with WikiParser {

  

  "parser" should "parse internal links" in {
    val s = "'''Anarchism''' is a [[political philosophy]] that advocates [[self-governance|self-governed]] societies"

    extractText(s) should equal("Anarchism is a political philosophy that advocates self-governed societies")
  }


  "parser" should "replace formatting recursivelly" in {
    val s = s"""
           {{Abraham Lincoln}}
          {{Navboxes
          |title=Offices and distinctions
          |list1=
          {{s-start}}
          {{s-par|us-hs}}
          {{s-bef|before=[[John Henry (representative)|John Henry]]}}
          {{s-ttl|title=Member of the [[List of United States Representatives from Illinois|House of Representatives]]&lt;br /&gt;from [[Illinois's 7th congressional district]]|years=1847–1849}}
          {{s-aft|after=[[Thomas L. Harris|Thomas Harris]]}}
          {{s-break}}
          {{s-ppo}}
          {{s-bef|before=[[John C. Frémont|John Frémont]]}}
          {{s-ttl|title=[[Republican Party (United States)|Republican]] [[List of United States Republican Party presidential tickets|nominee]] for&lt;br /&gt;President of the United States |years=[[United States presidential election, 1860|1860]], [[United States presidential election, 1864|1864]]}}
          {{s-aft|after=[[Ulysses S. Grant|Ulysses Grant]]}}
          {{s-break}}
          {{s-off}}
          {{s-bef|before=[[James Buchanan]]}}
          {{s-ttl|title=President of the United States|years=1861–1865}}
          {{s-aft|after=[[Andrew Johnson]]}}
          {{s-break}}
          {{s-hon}}
          {{s-bef|before=[[Henry Clay]]}}
          {{s-ttl|title=Persons who have [[lying in state|lain in state or honor]]&lt;br /&gt;in the [[United States Capitol rotunda]]|years=1865}}
          {{s-aft|after=[[Thaddeus Stevens]]}}
          {{s-end}}
          {{Hall of Fame for Great Americans}}
          }}
      """

    val result = extractText(s)
    println(s"RESULT: ${result.trim}")

    result shouldBe empty
    result shouldNot contain("{{")
    result shouldNot contain("}}")

  }


  it should "extract all wrapped text" in {
    val text = Source.fromFile("src/test/resources/test3.txt").getLines().mkString("\n")

    val parsedText = extractText(text)
    parsedText shouldNot contain("{{")
    parsedText shouldNot contain("}}")
  }

  it should "extract all wrapped confucianism" in {
    val text = Source.fromFile("src/test/resources/confucianism.txt").getLines().mkString("\n")

    val parsedText = extractText(text)
    parsedText shouldNot contain("{{")
    parsedText shouldNot contain("}}")

    println(parsedText)
  }



  it should "parse incorrect data" in {
    val text = """* {{cite web | title=Department of Anthropology | url=http://anthropology.si.edu/ | publisher=Smithsonian National Museum of Natural History | accessdate=25 March 2015}}
                   * {{cite web | url=https://aio.therai.org.uk/aio.php | title=AIO Home |

                   {{Social sciences}}
        """

    failAfter(1 seconds) {
      extractText(text) shouldNot contain("{{")
    }


  }


}
