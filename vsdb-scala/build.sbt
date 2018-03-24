organization := "at.hazm"
name := "anthedium"
version := "0.1.0"
scalaVersion := "2.12.4"
description := "vector-space database"
libraryDependencies ++= Seq(
  "javax.json" % "javax.json-api" % "1.1.2",
  "org.msgpack" % "msgpack" % "0.6.12",
  "org.slf4j" % "slf4j-log4j12" % "1.7.+",
"org.scalatest" %% "scalatest" % "3.0.+" % Test
)