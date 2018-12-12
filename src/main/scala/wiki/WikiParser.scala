package wiki

import fs2.Pipe

import scala.xml._
import fs2.{Pipe, Segment, Sink, io, text, Stream => S}
import wiki.utils.WrappersUtils
import scala.util.matching._

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}
import scala.collection.mutable.Stack

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

  }

}


object Parser extends WikiParser {

  def format[F[_]]: Pipe[F, Page, String] = _.flatMap(s => S.emit(formatPage(s)))

  def extractText[F[_]]: Pipe[F, Page, Try[Page]] = _.flatMap(s => S.emit(removeFormatting(s)))

  def toPage[F[_]]: Pipe[F, String, Try[Page]] = _.flatMap(s => S.emit(parse(s)))

  def wikiFilter(p: Page): Boolean = !p.text.toUpperCase.contains("#REDIRECT") && !p.title.toUpperCase.contains("(DISAMBIGUATION)")

  @deprecated("IO should be separated")
  def logProgress[F[_]]: Pipe[F, Page, Page] = _.zipWithIndex.map{
    case (p, id) =>
      val count = id + 1
      if(count % 10L == 0)  println(s"PAGES PROCESSED: $count")
      p
  }

  def log[F[_]]: Pipe[F, (Page, Long), Page] = _.through(s => s.map(_._1))



}