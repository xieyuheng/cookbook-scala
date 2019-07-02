organization := "xieyuheng"
name := "cookbook"
version := "0.0.1"
scalaVersion := "2.12.8"
lazy val akkaVersion = "2.5.23"
lazy val akkaHttpVersion = "10.1.8"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
libraryDependencies += "com.typesafe.slick" %% "slick" % "3.3.0"
libraryDependencies += "com.h2database" % "h2" % "1.4.197"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
