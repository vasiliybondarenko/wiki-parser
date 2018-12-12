package wiki


import org.scalatest.time.Span
import scalaz.concurrent.Task

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.{Await, Future}

/**
  * Created by Bondarenko on 8/20/18.
  */
trait Timed {

  def failAfter[T](time: Span)(f: =>T) = {

    Task(f).runFor(time)


  }


}
