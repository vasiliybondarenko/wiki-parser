package wiki

import cats.effect.{Blocker, IO}
import fs2.{io, text, Stream => S}
import java.nio.file.Paths
import cats.implicits._
import scala.concurrent.ExecutionContext.Implicits.global
import cats.effect.IO.contextShift

/**
 * Created by Bondarenko on Dec, 09, 2019
 * 13:02.
 * Project: Wikipedia
 */
object FileLinesSplitter extends App {

  implicit val cs = contextShift(global)
  val blocker = Blocker.liftExecutionContext(global)

  def fromFile(sourcePath: String) =
    io.file
      .readAll[IO](Paths.get(sourcePath), blocker, 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .intersperse("\n")

  def nextStream(s: S[IO, String], chunkNumber: Int) =
    s.drop(chunkNumber * 1000000).take(1000000)

  def split(path: String) =
    Range(0, 70).foreach { orderId =>
      val s = fromFile(path)
      nextStream(s, orderId)
        .through(text.utf8Encode)
        .through(io.file.writeAll(Paths.get(s"csv/$orderId.csv"), blocker))
        .compile
        .drain
        .unsafeRunSync()

      println(s"$orderId is completed")
    }

  split("neo4j.csv")

}
