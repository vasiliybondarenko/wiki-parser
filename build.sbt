import Dependencies._

lazy val root = (project in file(".")).settings(
  inThisBuild(
    List(
      organization := "com.example",
      scalaVersion := "2.12.8",
      version := "0.1.0-SNAPSHOT"
    )
  ),
  name := "Wikipedia",
  resolvers += Resolver.mavenLocal,
  resolvers += "Scalaz Bintray Repo".at("http://dl.bintray.com/scalaz/releases"),
  libraryDependencies += scalaTest % Test,
  libraryDependencies ++= Seq(
    "co.fs2" %% "fs2-core" % "2.1.0",
    "co.fs2" %% "fs2-io" % "2.1.0",
    "org.mongodb.scala" %% "mongo-scala-driver" % "2.7.0",
    "org.typelevel" %% "cats-core" % "2.0.0",
    "org.typelevel" %% "cats-free" % "1.0.1",
    "org.typelevel" %% "cats-effect" % "2.0.0",
    "net.debasishg" %% "redisclient" % "3.20",
    //"com.rabbitmq" %% "amqp-client" % "5.8.0",
    "dev.profunktor" % "fs2-rabbit_2.12" % "2.1.1",
    "org.neo4j.driver" % "neo4j-java-driver" % "4.0.0-rc1",
    "com.nrinaudo" %% "kantan.csv" % "0.6.0",
    "com.nrinaudo" %% "kantan.csv-cats" % "0.6.0",
    "com.nrinaudo" %% "kantan.csv-generic" % "0.6.0",
    "com.nrinaudo" %% "kantan.csv-refined" % "0.6.0"
  )
)
