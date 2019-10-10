  import Dependencies._

  val sparkVersion = "1.6.1"

  lazy val root = (project in file(".")).
    settings(
      inThisBuild(List(
        organization := "com.example",
        scalaVersion := "2.11.8",
        version      := "0.1.0-SNAPSHOT"
      )),
      name := "Wikipedia",
      resolvers += Resolver.mavenLocal,
      resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",

      libraryDependencies += scalaTest % Test,



      libraryDependencies ++= Seq(
        "org.apache.spark" %% "spark-core" % sparkVersion,
        "org.apache.spark" %% "spark-streaming" % sparkVersion,
        "org.scalaz.stream" %% "scalaz-stream" % "0.7a",
        // available for Scala 2.11, 2.12
        "co.fs2" %% "fs2-core" % "0.10.4",
        "co.fs2" %% "fs2-io" % "0.10.4",

        "org.mongodb.scala" %% "mongo-scala-driver" % "2.7.0",

        "org.typelevel" %% "cats-core" % "2.0.0",
        "org.typelevel" %% "cats-free" % "1.0.1",
        "org.typelevel" %% "cats-effect" % "1.0.0-RC"
      )

    )
