package wiki.utils

import scala.annotation.tailrec

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
  final def brackets(begin: String, end: String)(s: StringBuilder, initial: Stream[Bracket], startIndex: Int = 0): Stream[Bracket] = {
    val o1 = s.indexOf(begin, startIndex)
    val c1 = s.indexOf(end, startIndex)
    val opened = if (o1 != -1) Some(Opened(o1)) else None
    val closed = if (c1 != -1) Some(Closed(c1)) else None

    val (newState, newStartIndex) =
      (opened, closed) match {
        case (Some(o), Some(c)) =>
          if (o1 < c1) (initial #::: Stream[Bracket](o), o1 + begin.length) else {
            (initial #::: Stream[Bracket](c), c1 + end.length)
          }
        case (Some(o), None) => initial #::: Stream[Bracket](o) -> (o.index + begin.length)
        case (None, Some(c)) => initial #::: Stream[Bracket](c) -> (c.index + end.length)
        case _ => initial -> s.length
      }

    if (newStartIndex == s.length) newState else brackets(begin, end)(s, newState, newStartIndex)
  }


  final def replace(begin: String, end: String)(s: StringBuilder, opened: List[Int]): StringBuilder = {
    @tailrec
    def replaceRec(s: StringBuilder, opened: List[Int], allBrackets: Stream[Bracket]): StringBuilder = {
      val result =
        allBrackets match {
          case Stream.Empty => Right(s)
          case b #:: restBrackets => b match {
            case Opened(i) => Left(i :: opened)
            case Closed(i) if (!opened.isEmpty) => opened match {
              case firstOpened :: Nil =>
                Right(s.replace(firstOpened, i + end.length, ""))
              case _ :: restOpened =>
                if(restBrackets.isEmpty) {
                  val beginIndex = restOpened.last
                  val endIndex = Some(s.indexOf("\n", beginIndex)).filter(_ > -1).getOrElse(s.length)
                  Right(s.replace(beginIndex, endIndex, ""))
                } else Left(restOpened)
            }
          }
        }
      result match {
        case Right(r) => r
        case Left(opens) if(!opens.isEmpty) => replaceRec(s, opens, allBrackets.tail)
      }
    }

    val allBrackets = brackets(begin, end)(s, Stream.empty)
    replaceRec(s, opened, allBrackets)
  }

  @tailrec
  final def replaceAll(begin: String, end: String)(s: StringBuilder): StringBuilder = {
    implicit def opt(i: Int): Option[Int] = if(i > -1) Some(i) else None

    val result = replace(begin, end)(s, Nil)
    val beginIndex = result.indexOf(begin)
    if(beginIndex == -1) {
      result
    } else {
      opt(result.indexOf(end, beginIndex)) match {
        case None =>
          s.replace(beginIndex, result.indexOf("\n", beginIndex).getOrElse(result.length), "")
        case _ => replaceAll(begin, end)(result)
      }

    }
  }



}
