package wiki

import cats.Show
import cats.effect.Sync
import cats.implicits._
import java.io.{File, PrintStream}

import fs2.{Segment, Sink}

import scala.util.{Failure, Success, Try}

/**
 * Created by Bondarenko on 5/3/18.
 */
object IOUtils {

  def deleteFile(path: String) = {
    val file = new File(path)
    if (file.exists()) file.delete() else true
  }

  def apply[F[_], I](f: I => F[Unit]): Sink[F, I] = _.evalMap(f)

  def lines[F[_]](out: PrintStream)(implicit F: Sync[F]): Sink[F, String] =
    apply(str => F.delay(out.println(str)))

  def writeSegments[F[_]](outFileName: List[String] => String)(implicit F: Sync[F]): Sink[F, Segment[String, Unit]] =
    apply(
      seg =>
        F.delay {
          val lines = seg.force.toList
          val out = new PrintStream(outFileName(lines))
          lines.foreach(out.println)
          out.close()
        }
    )

}
