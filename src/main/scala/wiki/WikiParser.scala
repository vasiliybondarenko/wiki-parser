package wiki

import cats.effect.IO
import fs2.{ Pipe, Sink, Stream => S }
import org.bson.{ BsonElement, BsonString }
import org.mongodb.scala.Document
import org.mongodb.scala.bson.{ BsonDocument, BsonInt64 }
import wiki.mongo.MongoApp
import wiki.utils.WrappersUtils
import scala.annotation.tailrec
import scala.util.Try
import scala.xml._
import scala.collection.JavaConverters._

/**
  * Created by Bondarenko on 5/9/18.
  */
trait WikiParser extends Replacers {

  def parse(text: String): Try[Page] = {
    Try(XML.loadString(text)).flatMap { elem =>
      Try {
        val title = (elem \\ "page" \\ "title").text
        val pageContent = (elem \\ "page" \\ "text").text
        Page(title, pageContent)
      }
    }
  }


  def formatPage(p: Page) = {
    s"""
    |------------------------------------------------------
    |${p.title}
    |
    |${p.text}
    """.stripMargin('|')
  }

  def extractText(rawText: String) =
    rawText.
      replaceMarkups
      .replaceFonts
      .replaceOther
      .replaceLinks
      .replaceBold
      .replaceItalic
      .removeFurtherReading
      .removeReferences
      .removeSeeAlso
      .removeRefs
      .removeExternalLinks
      .removeTags

  def removeFormatting(p: Page): Try[Page] = {
    Try(p.copy(text = extractText(p.text)))
  }


}

case class Page(title: String, text: String)

trait Replacers extends WrappersUtils{

  implicit class Replacer(text: String) {

    def replaceLinks: String = {
      val p1 = "(.*)\\|(.*)".r
      val p2 = "(.*)".r
      replaceWrapping(text, "[[", "]]") {
        _ match {
          case p1(_, link) => link
          case p2(link) => link
        }
      }
    }



    def replaceBold = replaceWrapping(text, "'''", "'''")(s => s)

    def replaceItalic = replaceWrapping(text, "''", "''")(s => s)

    def replaceMarkups = replaceAll("{{", "}}")(new StringBuilder(text)).toString().trim

    def replaceFonts = replaceAll("{|", "}")(new StringBuilder(text)).toString().trim

    def replaceOther = replaceAll("{", "}")(new StringBuilder(text)).toString().trim

    def removeReferences = removeSection(text)("References")

    def removeSeeAlso = removeSection(text)("See also")

    def removeFurtherReading = removeSection(text)("Further reading")

    def removeExternalLinks = removeSection(text)("External links")

	def removeRefs = {
      replaceWrappingSeq(text)(
        List(
          "&lt;ref" -> "&lt;/ref",
          "&lt;ref" -> "/&gt;",
          "<ref" -> "</ref>",
          "<ref" -> "/>",
          "<!--" -> "-->"
        )
      )
    }

    def removeTags = {
      replaceWrappingSeq(text)(
        List(
          "<gallery" -> "</gallery>",
          "<center" -> "</center>",
          "<abbr" -> "</abbr>",
          "<div" -> "</div>",
          "<sub" -> "</sub>",
          "<sup" -> "</sup>",
          "<math" -> "</math>"
        )
      )
    }

    private def removeSection(text: String)(section: String) = {
      removeAfter(removeAfter(text, s"==$section=="), s"== $section ==")
    }

    private final def replaceWrappingSeq(text: String)(wrappings: List[(String, String)]) = {
      wrappings.foldLeft(text){
        case (prevText, (begin, end)) => replaceWrapping(prevText, begin, end)(_ => "")
      }
    }

    private final def replaceWrapping(text: String, start: String, end: String)(f: String => String): String = {
      val startIndex = text.indexOf(start, 0)
      val endIndex = text.indexOf(end, startIndex + start.length)

      if (startIndex == -1 || endIndex == -1) text
      else {
        val inner = text.substring(startIndex + start.length, endIndex)
        val result = new StringBuilder(text)
        replaceWrapping(result.replace(startIndex, endIndex + end.length, f(inner)).toString(), start, end)(f)
      }
    }


//    private final def removeTags(text: String)(f: String => String): String = {
//      val p = "<([a-zA-Z]+)(.*)".r
//      text match {
//        case p(tag, rest) =>
//          println(s"TAG: $tag")
//          val start = s"<$tag"
//          val end = s"</$tag>"
//          val startIndex =  text.indexOf(s"<$tag", 0)
//          val endIndex = rest.indexOf(end, startIndex + start.length)
//          if (startIndex == -1 || endIndex == -1) text
//          else {
//            val inner = text.substring(startIndex + start.length, endIndex)
//            val result = new StringBuilder(text)
//            removeTags(
//              result.replace(startIndex, endIndex + end.length, f(inner)).toString()
//            )(f)
//          }
//
//        case _ => text
//      }
//
//    }

    private final def removeAfter(text: String, after: String) = {
      val startIndex = text.indexOf(after)
      if(startIndex != -1) text.substring(0, startIndex).trim
      else text.trim
    }

  }

}


object Parser extends WikiParser {

  def withoutId[F[_]]: Pipe[F, (Long, Page), Page] = _.flatMap(s => S.emit(s._2))

  def format[F[_]]: Pipe[F, Page, String] = _.flatMap(s => S.emit(formatPage(s)))

  def extractText[F[_]]: Pipe[F, Page, Try[Page]] = _.flatMap(s => S.emit(removeFormatting(s)))

  def toPage[F[_]]: Pipe[F, String, Try[Page]] = _.flatMap(s => S.emit(parse(s)))

  def wikiFilter(p: Page): Boolean =
    !p.text.toUpperCase.contains("#REDIRECT") &&
      !p.title.toUpperCase.contains("(DISAMBIGUATION)") &&
      !p.title.contains("Wikipedia:WikiProject")

  @deprecated("IO should be separated")
  def logProgress[F[_]](implicit nanoStart: Long): Pipe[F, Page, (Long, Page)] = _.zipWithIndex.map{
    case (p, id) =>
      val count = id + 1
      if(count % 1000L == 0)
        println(s"PAGES PROCESSED: $count,  AVG TIME: ${count.toDouble * 1000000000.0 / (System.nanoTime() - nanoStart) } pages per sec")
      id -> p
  }

  private def pageToDoc(id: Long, p: Page): Document = {
    val elements = List(
      new BsonElement("id", new BsonInt64(id)),
      new BsonElement("title", new BsonString(p.title)),
      new BsonElement("body", new BsonString(p.text))
    )
    new Document(new BsonDocument(elements.asJava))
  }

  def saveToMongoDB[F[_]]: Sink[IO, (Long, Page)] = Sink[IO, (Long, Page)]{ p =>
    MongoApp.writeDoc("articles")(p){
      case (id, p) =>  pageToDoc(id, p)
    }.map(_ => ())
  }

  def writeToMongo[F[_]]: Pipe[F, Page, Page] = _.zipWithIndex.map{
    case (p, id) =>
      val count = id + 1
      MongoApp.write("wiki1")(List(p.title -> p.text))
      p
  }

  def log[F[_]]: Pipe[F, (Page, Long), Page] = _.through(s => s.map(_._1))



}