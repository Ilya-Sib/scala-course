ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.7"

lazy val root = (project in file("."))
  .settings(
    name := "TextClassifier"
  )

// https://mvnrepository.com/artifact/au.com.dius.pact.consumer/specs2
libraryDependencies += "au.com.dius.pact.consumer" %% "specs2" % "4.2.17"
