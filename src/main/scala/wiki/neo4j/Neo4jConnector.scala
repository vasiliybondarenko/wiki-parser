package wiki.neo4j

import cats.effect.{IO, Resource, Sync}
import org.neo4j.driver.{AuthTokens, Driver, GraphDatabase}
import wiki.Usage
import scala.collection.JavaConverters._
import scala.io.Source
import scala.util.Try

/**
 * Created by Bondarenko on Nov, 29, 2019
 10:49.
 Project: Wikipedia
 */
object Neo4jConnector extends App {

  def save[F[_]](usage: Usage)(implicit F: Sync[F]) =
    F.delay {
      Try(saveQuery(usage).flatMap(q => driver.session().run(q).asScala.toList))
    }

  def log[A](f: => A)(m: => String) = {
    val res = f
    println(m)
    res
  }

  private def prepareValue(s: String) =
    s.replaceAllLiterally("'", "").replaceAllLiterally(""""""", "")

  private def saveQuery(usage: Usage) =
    usage.links match {
      case Nil =>
        List(
          s"MERGE (p1: Page {value: '${usage.pageTitle.toLowerCase}', text: '${usage.sentences.mkString("\n")}'})"
        )
      case links =>
        links.zipWithIndex
          .map {
            case (l, i) =>
              s"""
		   |MERGE (p1_$i: Page {value: '${prepareValue(usage.pageTitle.toLowerCase)}'})
		   |MERGE (p2_$i: Page {value: '${prepareValue(l.toLowerCase)}'})
		   |CREATE (p1_$i)-[:LINKS_TO {}]->(p2_$i)
		   |""".stripMargin
          }

    }

  private lazy val driver =
    GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "1234"))

  val query = Source.fromFile("neo4j.sample.txt").getLines().mkString("\n")

  println(query)

  //run[IO](query).unsafeRunSync().foreach(println)

}
