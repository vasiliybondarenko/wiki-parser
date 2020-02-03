package wiki.redis

import cats.effect.{IO, Resource}
import com.redis._
import cats.effect._
import scala.collection.mutable.{Map => MMap}

/**
 * Created by Bondarenko on Jan, 24, 2020
 13:20.
 Project: Wikipedia
 */
object Redis extends App {
  //IO(println("")).start

  def redisClient = new RedisClient("localhost", 6379)

  val clients = new RedisClientPool("localhost", 6379)

  def mergeWords(words: MMap[String, Int]) =
    Resource
      .make(IO(redisClient))(c => IO(c.close()))
      .use { client =>
        IO {

          words.map { case (w, count) => client.pipeline(_.incrby(w, count)) }
        }
      }
      .map(_ => ())

  //    IO {
//      words.foreach {
//        case (w, count) =>
//          redisClient.pipeline(_.incrby(w, count))
//
//      }
//
//    }

  def byKey(w: String) = clients.withClient { client =>
    client.get(w)
  }

  mergeWords(MMap("Load" -> 1)).unsafeRunSync()
  mergeWords(MMap("Load" -> 2)).unsafeRunSync()
  mergeWords(MMap("ReLoad" -> 1)).unsafeRunSync()

  println(byKey("Load"))
  println(byKey("ReLoad"))

}
