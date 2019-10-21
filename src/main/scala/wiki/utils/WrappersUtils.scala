package wiki.utils

import scala.annotation.tailrec
import cats.data._
import cats.data.Chain._

/**
  * Created by Bondarenko on 8/17/18.
  */
trait WrappersUtils {

  //implicit def toSB(s: String) = new StringBuilder(s)

  sealed trait Bracket {
    val index: Int
  }

  case class Opened(index: Int) extends Bracket

  case class Closed(index: Int) extends Bracket

  @tailrec
  final def brackets(begin: String, end: String)(
    s: StringBuilder,
    initial: List[Bracket],
    startIndex: Int = 0
  ): List[Bracket] = {
    val o1 = s.indexOf(begin, startIndex)
    val c1 = s.indexOf(end, startIndex)
    val opened = if (o1 != -1) Some(Opened(o1)) else None
    val closed = if (c1 != -1) Some(Closed(c1)) else None

    val (newState, newStartIndex) =
      (opened, closed) match {
        case (Some(o), Some(c)) =>
          if (o1 < c1) (initial ::: List[Bracket](o), o1 + begin.length)
          else {
            (initial ::: List[Bracket](c), c1 + end.length)
          }
        case (Some(o), None) =>
          (initial ::: List[Bracket](o)) -> (o.index + begin.length)
        case (None, Some(c)) =>
          (initial ::: List[Bracket](c)) -> (c.index + end.length)
        case _ => initial -> s.length
      }

    if (newStartIndex == s.length) newState
    else brackets(begin, end)(s, newState, newStartIndex)
  }

  final def replace(
    begin: String,
    end: String
  )(s: StringBuilder, opened: List[Int]): StringBuilder = {

    @tailrec
    def replaceRec(s: StringBuilder,
                   opened: List[Int],
                   allBrackets: Chain[Bracket]): StringBuilder = {

      val result =
        allBrackets match {
          case x if x.isEmpty => Right(s)
          case b ==: restBrackets =>
            b match {
              case Opened(i) => Left(i :: opened)
              case Closed(i) if !opened.isEmpty =>
                opened match {
                  case firstOpened :: Nil =>
                    Right(s.replace(firstOpened, i + end.length, ""))
                  case _ :: restOpened =>
                    if (restBrackets.isEmpty) {
                      val beginIndex = restOpened.last
                      val endIndex = Some(s.indexOf("\n", beginIndex))
                        .filter(_ > -1)
                        .getOrElse(s.length)
                      Right(s.replace(beginIndex, endIndex, ""))
                    } else Left(restOpened)
                }
              case Closed(_) => Right(s)
            }
        }

      val restBrackets = allBrackets match {
        case _ ==: rest     => rest
        case x if x.isEmpty => Chain.empty[Bracket]
      }

      result match {
        case Right(r) => r
        case Left(opens) if (!opens.isEmpty) =>
          replaceRec(s, opens, restBrackets)
      }
    }

    val allBrackets = brackets(begin, end)(s, List.empty)
    replaceRec(s, opened, Chain.fromSeq(allBrackets))
  }

  @tailrec
  final def replaceAll(begin: String, end: String, log: Boolean = false)(
    s: StringBuilder
  ): StringBuilder = {
    implicit def opt(i: Int): Option[Int] = Some(i).filter(_ > -1)

    val prevResultSize = s.length
    val result = replace(begin, end)(s, Nil)
    val beginIndex = result.indexOf(begin)

    if (beginIndex == -1 || prevResultSize == result.length) {
      result
    } else {
      opt(result.indexOf(end, beginIndex)) match {
        case None =>
          s.replace(
            beginIndex,
            result.indexOf("\n", beginIndex).getOrElse(result.length),
            ""
          )
        case _ =>
          replaceAll(begin, end, log)(result)
      }

    }
  }

}
