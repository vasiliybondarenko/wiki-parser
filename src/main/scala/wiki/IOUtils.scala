package wiki

import cats.Show
import cats.effect.{ContextShift, Sync}
import cats.implicits._
import java.io.{File, PrintStream}
import fs2.{io, Chunk, Pipe, Sink}
import wiki.Main.blocker
import java.nio.file.Paths
import scala.util.{Failure, Success, Try}

/**
 * Created by Bondarenko on 5/3/18.
 */
object IOUtils {

  def bytesWriter[F[_]: Sync: ContextShift](path: String) =
    io.file.writeAll[F](Paths.get(path), blocker)

  def deleteFile(path: String) = {
    val file = new File(path)
    if (file.exists()) file.delete() else true
  }

  def apply[F[_], I](f: I => F[Unit]): Pipe[F, I, Unit] = _.evalMap(f)

  def lines[F[_]](out: PrintStream)(implicit F: Sync[F]): Pipe[F, String, Unit] =
    apply(str => F.delay(out.println(str)))

}
