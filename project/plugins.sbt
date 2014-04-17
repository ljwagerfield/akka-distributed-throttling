import sbt._
import Defaults._

scalacOptions += "-unchecked"

scalacOptions += "-feature"

scalacOptions += "-deprecation"

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.4.0")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.7.0-SNAPSHOT")

resolvers ++= Seq("sonatype-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
                  "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/")
