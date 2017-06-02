name := """volg-je-vrienden"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.0",
  "org.xerial" % "sqlite-jdbc" % "3.8.11.2",
  "commons-io" % "commons-io" % "2.5"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
