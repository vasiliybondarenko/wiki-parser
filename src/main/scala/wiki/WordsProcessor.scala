package wiki

import cats.effect.IO
import fs2.{Sink, Stream => S}
import wiki.mongo.{MongoApp, MongoSerdes, Word}
import scala.collection.JavaConverters._
import scala.collection.mutable.{Map => MMap}

/**
 * Created by Bondarenko on Jan, 06, 2020
 * 15:52.
 * Project: Wikipedia
 */
case class Doc(id: Int, title: String, body: Array[String])

trait WordsCountsHelper {

  def words(lines: Array[String]) = {
    val counts =
      lines.toSeq
        .map { usage =>
          wordsCounts(parseWords(usage))
        }
    if (counts.isEmpty) MMap.empty[String, Int]
    else counts.reduce(mergeCounts)
  }

  def countWords(lines: Array[String]) = {
    val counts =
      lines.toSeq
        .map { usage =>
          wordsCounts(parseWords(usage))
        }
    counts
  }

  def mergeCounts(a: MMap[String, Int], b: MMap[String, Int]) = {

    val counts = collection.mutable.Map.empty[String, Int]
    a.foreach {
      case (w, c) => counts.put(w, c)
    }

    b.foreach {
      case (w, count) => counts.put(w, counts.get(w).getOrElse(0) + count)
    }
    counts

  }

  def saveWords(words: MMap[String, Int]) = {
    val serde = MongoSerdes.WordsSerde
    val wordCounts = words.map { case (w, count) => Word(w, count.toLong) }
    MongoApp.writeDocs("words")(wordCounts.toSeq)(serde.toMongoDoc)
  }

  def parseWords(s: String): Seq[String] = {
    val pattern = "[a-zA-Z]+".r
    pattern
      .findAllMatchIn(s.replaceAll("http://[^ ]+", ""))
      .map(_.group(0))
      .map(_.toLowerCase)
      .toSeq
  }

  private def wordsCounts(words: Seq[String]) = {
    val counts = collection.mutable.Map.empty[String, Int]
    words.foreach { w =>
      val count = counts.get(w).getOrElse(0)
      counts.put(w, count + 1)
    }
    counts
  }

}

object WordsProcessor extends App with WordsCountsHelper {

  val batchSize = 1000

  def readFromMongo(offset: Int) =
    MongoApp.getDocuments("usages")(offset, batchSize).map { d =>
      Doc(
        d.getLong("id").toInt,
        d.getString("title"),
        d.getList("body", Class.forName("java.lang.String")).asScala.toArray.map(_.toString)
      )
    }

  def showSink[A]() = Sink[IO, A] { x =>
    IO { println(x) }
  }

  def saveToMongo() = Sink[IO, MMap[String, Int]] { words =>
    saveWords(words).map(_ => ())
  }

  //saveWords(Map("xxx" -> 1))

  S.range(0, 31000, batchSize)
    .map { offset =>
      readFromMongo(offset)
        .map { d =>
          words(d.body)
        }
        .reduce(mergeCounts)
    }
    .covary[IO]
    .map { x =>
      println(s"${x.size}")
      x
    }
    .zipWithIndex
    .fold(MMap.empty[String, Int]) { (a, b) =>
      val mergedCounts = mergeCounts(a, b._1)
      println(s"[${b._2}] ${mergedCounts.size}")
      mergedCounts
    }
    .through(saveToMongo())
    .compile
    .drain
    .unsafeRunSync()

}
