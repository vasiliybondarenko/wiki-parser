package wiki.mongo

import org.mongodb.scala.Document
import wiki.Usage
import org.bson.{BsonArray, BsonElement, BsonString}
import org.mongodb.scala.bson.{BsonDocument, BsonInt64}
import org.mongodb.scala.model.Filters
import org.mongodb.scala.{Document, MongoClient}
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

/**
 * Created by Bondarenko on Oct, 24, 2019
 * 11:07.
 * Project: Wikipedia
 */
object MongoSerdes {

  implicit object UsagesSerde extends MongoSerde[Usage] {
    def toMongoDoc(data: Usage): Document = {
      val elements = List(
        new BsonElement("id", new BsonInt64(data.id)),
        new BsonElement("pageId", new BsonInt64(data.pageId)),
        new BsonElement("title", new BsonString(data.pageTitle)),
        new BsonElement("body", new BsonArray(data.sentences.map(new BsonString(_)).toList.asJava))
      )
      new Document(new BsonDocument(elements.asJava))
    }

    def mongoCollection: String = "usages.temp"
  }

  implicit object WordsSerde extends MongoSerde[Word] {
    def toMongoDoc(data: Word): Document = {
      val elements = List(
        new BsonElement("value", new BsonString(data.value)),
        new BsonElement("count", new BsonInt64(data.count))
      )
      new Document(new BsonDocument(elements.asJava))
    }

    def mongoCollection: String = "words"
  }

}
