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
    val s =
      "'''Anarchism''' is a [[political philosophy]] that advocates [[self-governance|self-governed]] societies"

    extractText(s) should equal(
      "Anarchism is a political philosophy that advocates self-governed societies"
    )
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

    result shouldBe empty
    result shouldNot contain("{{")
    result shouldNot contain("}}")

  }

  it should "extract all wrapped text" in {
    val text =
      Source.fromFile("src/test/resources/test3.txt").getLines().mkString("\n")

    val parsedText = extractText(text)
    parsedText shouldNot contain("{{")
    parsedText shouldNot contain("}}")
  }

  it should "remove links sections" in {
    val text =
      Source.fromFile("src/test/resources/test4.txt").getLines().mkString("\n")

    val text1 = """
      == See also ==

      * Comparison of Linux distributions
      * Computer technology for developing areas
      * DCC Alliance
      * Free culture movement
    """
    val text2 =
      """
      == References ==
      * Walter Burkert (1985) Greek Religion, Harvard University Press, 1985.
    """

    val text3 =
      """
      == Further reading ==
      * Dalai Lama. (1991) Freedom in Exile: The Autobiography of the Dalai Lama. San Francisco, CA.
    """

    val parsedText = extractText(text)

    parsedText.indexOf("==See also==") shouldBe -1
    parsedText.indexOf("==References==") shouldBe -1
    parsedText.indexOf("==Further reading==") shouldBe -1

    extractText(text1) shouldBe ""
    extractText(text2) shouldBe ""
    extractText(text3) shouldBe ""

    parsedText should endWith(
      "Anarchism is often evaluated as unfeasible or utopian by its critics."
    )
  }

  it should "remove == References == section" in {
    val text = """  == Notes ==

      == References ==        
    """
    extractText(text) shouldBe ("== Notes ==")
  }

  it should "remove external links section" in {
    val text =
      """
            Constellations: An International Journal of Critical and Democratic Theory is a quarterly peer-reviewed academic journal of critical and democratic theory and successor of Praxis International. It is edited by Andrew Arato, Amy Allen, and Andreas Kalyvas. Seyla Benhabib is a co-founding former editor and Nancy Fraser a former co-editor.

            ==External links==
            *
            Category:Sociology journals
            Category:Publications established in 1994
            Category:Quarterly journals
            Category:Wiley-Blackwell academic journals
            Category:English-language journals
    """

    val parsedText1 = extractText(text)
    val parsedText2 =
      extractText(text.replace("==External links==", "== External links =="))

    parsedText1.trim shouldEqual (
      "Constellations: An International Journal of Critical and Democratic Theory is a quarterly peer-reviewed academic journal of critical and democratic theory and successor of Praxis International. It is edited by Andrew Arato, Amy Allen, and Andreas Kalyvas. Seyla Benhabib is a co-founding former editor and Nancy Fraser a former co-editor."
    )
    parsedText2.trim shouldEqual (
      "Constellations: An International Journal of Critical and Democratic Theory is a quarterly peer-reviewed academic journal of critical and democratic theory and successor of Praxis International. It is edited by Andrew Arato, Amy Allen, and Andreas Kalyvas. Seyla Benhabib is a co-founding former editor and Nancy Fraser a former co-editor."
    )

  }

  it should "remove ref elements encoded" in {
    val text =
      Source.fromFile("src/test/resources/test5.txt").getLines().mkString("\n")

    val parsedText = extractText(text)

    parsedText.indexOf("""&lt;ref name=&quot;constitution&quot; /&gt;""") shouldBe -1
    parsedText.indexOf("""&lt;/ref""") shouldBe -1

  }

  it should "remove ref elements" in {
    val text =
      """
      <ref name=slevin>Slevin, Carl. "Anarchism." The Concise Oxford Dictionary of Politics. Ed. Iain McLean and Alistair McMillan. Oxford University Press, 2003.</ref>
    """
    val parsedText = extractText(text)

    parsedText should be("")
  }

  it should "remove <!-- comments" in {
    val text =
      """
      <!-- Please refrain from name-dropping your favorite band; as we would all love to include our favorites, only a few examples are needed to benefit the article. -->
    """
    val parsedText = extractText(text)

    parsedText should be("")
  }

  it should "extract all wrapped confucianism" in {
    val text = Source
      .fromFile("src/test/resources/confucianism.txt")
      .getLines()
      .mkString("\n")

    val parsedText = extractText(text)
    parsedText shouldNot contain("{{")
    parsedText shouldNot contain("}}")
  }

  it should "parse wiki project page" in {
    val text = Source
      .fromFile("src/test/resources/wiki-project.txt")
      .getLines()
      .mkString("\n")

    val parsedText = extractText(text)

    parsedText should not be (empty)
  }

  it should "remove simple brackets sequences" in {
    val text =
      """
        |{{Use British English|date=January 2014}}
        |{{Anarchism sidebar}}
        |{{Basic forms of government}}
        |Anarchism is a political philosophy that advocates self-governed societies based on voluntary institutions. These are often described as stateless societies, although several authors have defined them more specifically as institutions based on non-hierarchical or free associations. Anarchism holds the state to be undesirable, unnecessary, and harmful.
    """.stripMargin
    val parsedText = extractText(text)

    parsedText should be(
      "Anarchism is a political philosophy that advocates self-governed societies based on voluntary institutions. These are often described as stateless societies, although several authors have defined them more specifically as institutions based on non-hierarchical or free associations. Anarchism holds the state to be undesirable, unnecessary, and harmful."
    )
  }

  //ignored due to slow performance
  ignore should "parse large text bodies without StackOverFlowException" in {
    val text = Source
      .fromFile("src/test/resources/large-text.txt")
      .getLines()
      .mkString("\n")

    val parsedText = extractText(text)

    parsedText should not be (empty)
  }

  it should "parse incorrect data" in {
    val text =
      """* {{cite web | title=Department of Anthropology | url=http://anthropology.si.edu/ | publisher=Smithsonian National Museum of Natural History | accessdate=25 March 2015}}
                   * {{cite web | url=https://aio.therai.org.uk/aio.php | title=AIO Home |

                   {{Social sciences}}
        """

    failAfter(1 seconds) {
      extractText(text) shouldNot contain("{{")
    }

  }

  it should "parse without exceptions real article" in {
    val text =
      Source.fromFile("src/test/resources/test3.txt").getLines().mkString("\n")

    val parsed = extractText(text).trim

    parsed shouldNot contain("{{")
    parsed shouldNot contain("}}")
    parsed should startWith("Animation is a dynamic medium")
    parsed should endWith("Annie Award for Best Animated Television Production")
  }

}
