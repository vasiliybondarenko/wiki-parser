package wiki

import java.nio.file.Paths
import cats.effect.IO
import fs2.Stream._
import fs2.{Pipe, Segment, io, text, Stream => S}
import wiki.IOUtils.deleteFile
import wiki.Parser._
import wiki.setups.Config
import wiki.setups.WikiConf.Conf
import wiki.mongo.MongoSerdes.UsagesSerde
import scala.io.Source
import scala.util.Success

/**
 * Created by Bondarenko on 5/27/18.
 */
object Main extends App {

  implicit val start = System.nanoTime()

  private def concat[F[_]]: Pipe[F, Segment[String, Unit], String] =
    _.flatMap(seg => S(seg.force.toList.mkString(delimiter.toString) + "</page>"))

  private def skipLine(line: String): Boolean = {
    val s = line.trim
    s.startsWith("{{") && s.endsWith("}}") ||
    s.startsWith("*") ||
    s.startsWith("|") ||
    s.startsWith("{|")
  }

  private def parsedWikiPages(config: Config) =
    io.file
      .readAll[IO](Paths.get(config.wikiPath), 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .dropWhile(!_.contains("<page"))
      .split(line => line.trim.startsWith("</page>"))
      .through(concat)
      .through(logProgress)
      .through(toUsage)
      .through(withId)

  def convertAndWriteToMongo(implicit config: Config) =
    parsedWikiPages(config)
      .to(saveToMongoDB)

  def convertAndWriteToFile(implicit config: Config) = {
    deleteFile(config.targetFilePath)
    parsedWikiPages(config)
      .through(usageToText)
      .through(text.utf8Encode)
      .to(io.file.writeAll(Paths.get(config.targetFilePath)))
  }

  convertAndWriteToFile.compile.drain.unsafeRunSync()

}
