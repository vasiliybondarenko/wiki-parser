package wiki

import org.apache.spark.{SparkConf, SparkContext}
import scalaz.concurrent.Task
import wiki.Reader.path
import scalaz.stream.io
import scalaz.stream.tee
import scalaz.stream.process1
import scalaz.stream.Process

import scala.io.Source

/**
  * Created by Bondarenko on 4/25/18.
  */
object Reader extends App {

  implicit class P(value: Any){
    def show() = println(value)
  }


  implicit lazy val sc = {
    val conf = new SparkConf().setAppName("Wiki").setMaster("local")

    new SparkContext(conf)
    
  }

  def parse(path: String, outputFolder: String) = {
    Source.fromFile(path).getLines().toStream



//    val stdout = Process constant (puts _) toSource
//
//    p to stdout

    ???
  }

  def testStreams(path: String) = {
    def puts(ln: String): Task[Unit] = Task { println(ln) }

    val gets: Task[String] = Task { Console.readLine() }

    

    //val p = Process eval puts("Hello, World!")

//    val stdout = Process constant (puts _) toSource
//
//    val lines = Process repeatEval gets
//
//
//    val p = lines flatMap { line =>
//      Process eval puts(line)
//    }


    val allLines = io.linesR(path)
   
    
    


    //val lines = Process repeatEval gets
    val stdout = Process constant (puts _) toSource

    //val p = lines to stdout

    val p2 = allLines to stdout
    p2.run.run

  }

  val path = "/Users/shredinger/Documents/DEVELOPMENT/Wikipedia/enwiki-20180120-pages-articles-multistream.xml"

  def testSpark() = {


    val lines = sc.textFile(path)
  }


  testStreams(path)









}
