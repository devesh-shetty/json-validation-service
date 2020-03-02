lazy val buildSettings = Seq(
  organization := "com.deveshshetty",
  name := "json-validation-service",
  version := "1.0.0",
  description := "A REST-service for validating JSON documents against JSON Schemas",
  scalaVersion := "2.13.1",
  PlayKeys.playDefaultPort := 80,
)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(buildSettings)
  .settings(
    libraryDependencies ++= Seq(
      guice,
      "com.github.java-json-tools" % "json-schema-validator" % "2.2.13",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
      "org.mockito" % "mockito-scala_2.13" % "1.11.4" % Test,
    )
  )