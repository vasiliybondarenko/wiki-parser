package example

import org.scalatest._
import wiki.Implicits._

import scala.io.Source

class HelloSpec extends FlatSpec with Matchers {
  "The Hello object" should "say hello" in {
    Hello.greeting shouldEqual "hello"
  }

  "aaa" should "parse" in {


    val text = Source.fromFile("/Users/shredinger/Documents/DEVELOPMENT/Projects/Wikipedia/src/test/resources/test1.xml").getLines().mkString("\n")

    text.replaceAll("\\{\\{([\\S|\\s]+)\\}\\}", "").show

  }


  "xxx" should "dsafsa" in {

  }
}
