name := "assignment-3"

version := "0.1"

scalaVersion := "3.3.5"

val pekkoVersion = "1.5.0"

libraryDependencies ++= Seq(
  "org.apache.pekko" %% "pekko-actor-typed" % pekkoVersion,
  "org.apache.pekko" %% "pekko-stream"      % pekkoVersion,
  "org.apache.pekko" %% "pekko-slf4j"       % pekkoVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.6",
  "ch.qos.logback" % "logback-classic" % "1.5.32"
)
