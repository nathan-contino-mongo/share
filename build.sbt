ThisBuild / scalaVersion := "2.12.7"
ThisBuild / organization := "com.mongodb"

lazy val root = (project in file("."))
  .settings(
    name := "MongoDBx509",
    libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "2.7.0",
  )
