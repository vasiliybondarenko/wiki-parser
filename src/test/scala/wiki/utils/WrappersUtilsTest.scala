package wiki.utils

import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

/**
  * Created by Bondarenko on 8/17/18.
  */
class WrappersUtilsTest extends FlatSpec with Matchers with WrappersUtils {

  implicit def toSb(s: String) = new StringBuilder(s)

  it should "return ordered sequence of brackets " in {
    brackets("{{", "}}")("{{load}}", List.empty) should equal(
      List(Opened(0), Closed(6))
    )
    brackets("{{", "}}")("{{{{load}}}}", List.empty) should equal(
      List(Opened(0), Opened(2), Closed(8), Closed(10))
    )
    brackets("{{", "}}")("{{}}", List.empty) should equal(
      List(Opened(0), Closed(2))
    )
    brackets("{{", "}}")("{{load{{reload}}}}", List.empty) should equal(
      List(Opened(0), Opened(6), Closed(14), Closed(16))
    )
    brackets("{{", "}}")("{{}}{{}}", List.empty) should equal(
      List(Opened(0), Closed(2), Opened(4), Closed(6))
    )

  }

  it should "parse simple brackets" in {
    replace("{{", "}}")(new StringBuilder("{{load}}"), Nil) shouldBe empty
    replace("{{", "}}")(new StringBuilder("{{{{load}}}}"), Nil) shouldBe empty
  }

  it should "parse real brackets sequence" in {
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
      """.trim

    replaceAll("{{", "}}")(s).toString().trim shouldBe empty

  }

  ignore should "parse without exceptions real article" in {
    val text =
      Source.fromFile("src/test/resources/test3.txt").getLines().mkString("\n")

    println(replaceAll("{{", "}}")(text).toString().trim)

  }

}
