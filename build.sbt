import Dependencies._
import sbt.Resolver

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.11.8",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "Hello",
    libraryDependencies += scalaTest % Test
  ).settings(resolvers ++= Seq(Resolver.bintrayRepo("hmrc", "releases"), Resolver.jcenterRepo))
  .settings(libraryDependencies += dataValidator)
  .settings(libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.2")
