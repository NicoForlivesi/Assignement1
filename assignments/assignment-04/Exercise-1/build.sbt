name := "assignment-4"
version := "0.1"
scalaVersion := "3.3.5"

lazy val pekkoVersion = "1.5.0"

libraryDependencies ++= Seq(
  "org.apache.pekko" %% "pekko-actor-typed"            % pekkoVersion,
  "org.apache.pekko" %% "pekko-stream"                 % pekkoVersion,
  "org.apache.pekko" %% "pekko-slf4j"                  % pekkoVersion,
  "org.apache.pekko" %% "pekko-cluster-typed"          % pekkoVersion,
  "org.apache.pekko" %% "pekko-cluster-sharding-typed" % pekkoVersion,
  "org.apache.pekko" %% "pekko-serialization-jackson"  % pekkoVersion,
  "ch.qos.logback" % "logback-classic" % "1.5.32"
)
