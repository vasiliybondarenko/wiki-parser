package wiki.mongo

import cats.effect.{ContextShift, IO}
import org.bson.conversions.Bson
import org.bson.{BsonElement, BsonString}
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.Filters
import org.mongodb.scala.{Document, MongoClient}
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.global
import scala.util.Try

/**
 * Created by Bondarenko on Sep, 23, 2019
 * 11:12 PM.
 * Project: Wikipedia
 */
object MongoApp extends App {
  implicit val cs: ContextShift[IO] = IO.contextShift(global)

  lazy val client = MongoClient("mongodb://localhost:27017")

  def getDocuments(collectionName: String)(startId: Int, count: Int) = {
    val coll = client.getDatabase("wikipedia").getCollection(collectionName)
    Await.result(
      coll.find(Filters.gte("id", startId)).limit(count).toFuture(),
      10.second
    )
  }

  def textSearch(collectionName: String)(search: String) = {
    val coll = client.getDatabase("test").getCollection(collectionName)
    Await
      .result(coll.find(Filters.text(s""""$search"""")).toFuture(), 10.seconds)
      .toStream
  }

  def write(collectionName: String)(values: List[(String, String)]) = {
    val coll = client.getDatabase("test").getCollection(collectionName)

    val docs = values.map {
      case (k, v) =>
        val elements = List(
          new BsonElement("key", new BsonString(k)),
          new BsonElement("value", new BsonString(v))
        )

        new Document(new BsonDocument(elements.asJava))
    }
    Await.result(coll.insertMany(docs).toFuture(), 10.seconds)
  }

  def writeDoc[T](collectionName: String)(doc: T)(f: T => Document) = {
    val coll = client.getDatabase("wikipedia").getCollection(collectionName)
    IO.fromFuture {
      IO {
        coll.insertOne(f(doc)).toFuture()
      }
    }
  }

  def writeDocs[T](collectionName: String)(docs: Seq[T])(f: T => Document) = {
    val coll = client.getDatabase("wikipedia").getCollection(collectionName)
    IO {
      Await.result(coll.insertMany(docs.map(f)).toFuture(), 10.seconds)
    }

  }

  textSearch("wiki1")("Enraged over").foreach { d =>
    println(d)
  }

}

case class Word(value: String, count: Long)

trait MongoSerde[A] {
  def toMongoDoc(data: A): Document
  def mongoCollection: String
}
