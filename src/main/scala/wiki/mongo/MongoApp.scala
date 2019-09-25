package wiki.mongo


import cats.effect.IO
import org.bson.conversions.Bson
import org.bson.{ BsonElement, BsonString }
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.Filters
import org.mongodb.scala.{ Document, MongoClient }
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.collection.JavaConverters._


/**
 * Created by Bondarenko on Sep, 23, 2019 
 * 11:12 PM.
 * Project: Wikipedia
 */
object MongoApp extends App{
  lazy val client =  MongoClient()



  private def show = {
    val coll = client.getDatabase("test").getCollection("test")
    val result = coll.find().toFuture()
    Await.result(result, 10.second).foreach(println)
  }

  def textSearch(collectionName: String)(search: String) = {
	val coll = client.getDatabase("test").getCollection(collectionName)
    Await.result(
      coll.find(Filters.text(s""""$search"""")).toFuture(),
      10.seconds
    ).toStream
  }

  def write(collectionName: String)(values: List[(String, String)]) = {
    val coll = client.getDatabase("test").getCollection(collectionName)

    val docs = values.map{ case (k, v) =>
      val elements = List(
        new BsonElement("key", new BsonString(k)),
        new BsonElement("value", new BsonString(v))
      )

      new Document(new BsonDocument(elements.asJava))
    }
    Await.result(coll.insertMany(docs).toFuture(), 10.seconds)
  }


  textSearch("wiki1")("Enraged over").foreach{ d  =>
    println(d)
  }

}


