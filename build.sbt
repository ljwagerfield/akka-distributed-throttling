name := "Akka Distributed Throttling"

version := "1.0"

scalacOptions += "-unchecked"

scalacOptions += "-feature"

scalacOptions += "-deprecation"

org.scalastyle.sbt.ScalastylePlugin.Settings

libraryDependencies += "org.specs2" %% "specs2" % "2.3.10" % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4-SNAPSHOT"

libraryDependencies += "com.typesafe.akka" %% "akka-cluster" % "2.4-SNAPSHOT"

libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.4-SNAPSHOT"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.1"

libraryDependencies += "com.netflix.rxjava" % "rxjava-scala" % "0.17.4"

resolvers += "typesafe-snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"
