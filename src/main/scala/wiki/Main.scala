package wiki

import cats.effect.IO.contextShift
import java.nio.file.Paths
import cats.effect.{Blocker, IO}
import fs2.Stream._
import fs2.{Chunk, Pipe, Sink, io, text, Stream => S}
import wiki.IOUtils.bytesWriter
import wiki.IOUtils.deleteFile
import wiki.Parser._
import wiki.setups.Config
import wiki.setups.WikiConf.Conf
import wiki.mongo.MongoSerdes.UsagesSerde
import scala.io.Source
import scala.util.Success
import wiki.neo4j.CSVExport._
import wiki.WikiFilters.excludeTitle
import scala.collection.mutable.{Map => MMap}
import wiki.WordsProcessor.saveWords
import wiki.redis.Redis
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Bondarenko on 5/27/18.
 */
object Main extends App with WordsCountsHelper {

  implicit val start = System.nanoTime()

  val blocker = Blocker.liftExecutionContext(global)

  implicit val cs = contextShift(global)

  private def concat[F[_]]: Pipe[F, Chunk[String], String] =
    _.flatMap(
      chunks =>
        S(chunks.foldLeft("") { (a, b) =>
          s"$a$delimiter$b"
        } + "</page>")
    )
  //_.flatMap(seg => S(seg.force.toList.mkString(delimiter.toString) + "</page>"))

  private def skipLine(line: String): Boolean = {
    val s = line.trim
    s.startsWith("{{") && s.endsWith("}}") ||
    s.startsWith("*") ||
    s.startsWith("|") ||
    s.startsWith("{|")
  }

  private def saveWordsCountsToMongo = Sink[IO, MMap[String, Int]] { words =>
    saveWords(words).map(_ => ())
  }

  private def saveWordsCountsToRedis = Sink[IO, MMap[String, Int]] { words =>
    Redis.mergeWords(words)
  }

  private def parsedWikiPages(config: Config) =
    io.file
      .readAll[IO](Paths.get(config.wikiPath), blocker, 4096)
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
      .through(saveToMongoDB)

  def wordsCountsToMongo(implicit config: Config) =
    parsedWikiPages(config)
      .flatMap { usage =>
        S.apply(countWords(usage.sentences): _*)
      }
      .take(1000000)
      .through(saveWordsCountsToRedis)

  def convertAndWriteToFile(implicit config: Config) = {
    deleteFile(config.targetFilePath)
    parsedWikiPages(config)
      .through(usageToText)
      .through(text.utf8Encode)
      .through(bytesWriter(config.targetFilePath))
  }

  def convertAndWriteToCSVForNeo4j(implicit config: Config) =
    parsedWikiPages(config)
      .filter { u =>
        !excludeTitle(u.pageTitle.toLowerCase)
      }
      .flatMap { s =>
        fs2.Stream.emits(s.links.map(l => s"${encode(s.pageTitle)},${encode(l)}\r\n"))
      }
      .through(text.utf8Encode)
      .through(bytesWriter("neo4j.csv"))

  def convertAndWriteToNeo4j(implicit config: Config) =
    parsedWikiPages(config)
      .through(saveToNeo4j)

  //convertAndWriteToFile.compile.drain.unsafeRunSync()
  //convertAndWriteToNeo4j.compile.drain.unsafeRunSync()
  //convertAndWriteToCSVForNeo4j.compile.drain.unsafeRunSync()

  wordsCountsToMongo.compile.drain.unsafeRunSync()

}
