lazy val buildSettings = Seq(
  organization  :=  "com.deveshshetty",
  name          :=  "json-validation-service",
  version       :=  "1.0.0",
  description   :=  "A REST-service for validating JSON documents against JSON Schemas",
  scalaVersion  :=  "2.13.1",
)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(buildSettings)