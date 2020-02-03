package wiki.neo4j

import fs2.Sink
import fs2.Stream._
import cats.effect.IO
import cats.effect.Resource
import cats.effect.Sync
import fs2.io.file.pulls
import java.io.File
import kantan.csv._
import kantan.csv.ops._
import kantan.csv.generic._
import wiki.Usage
import java.nio.file.{Paths, StandardOpenOption}

/**
 * Created by Bondarenko on Dec, 03, 2019
 10:13.
 Project: Wikipedia
 */
object CSVExport extends App {

  def encode(s: String): String = {
    val s1 =
      s.replaceAllLiterally(""""""", "")
        .replaceAllLiterally("'", "")
        .replaceAllLiterally("'", "\"")
        .replaceAllLiterally("`", "")
        .replaceAllLiterally(",", " ")
    s""""${s1.toLowerCase}""""

  }

  //todo: see also kantan.csv.engine.WriterEngine
  def saveToCSV(path: String): Sink[IO, Usage] = Sink[IO, Usage] { usage =>
    println(s"writing to file ...")
    writeUsage(usage)(new File(path))
  }

  def writeUsage(usage: Usage)(file: File) =
    IO.delay {
      val links = usage.links.map(l => usage.pageTitle -> l)
      file.asCsvWriter[(String, String)](rfc).write(links).close()
    }

  def write(path: String) = {
    val s =
      """sucker for pain,
        | no gain no pain, "refused to give up quick" """.stripMargin
    new File(path)
      .asCsvWriter[(String, String)](rfc)
      .write(s -> "Lie Wayne")
      .close()
  }

  write("test.csv")

}
