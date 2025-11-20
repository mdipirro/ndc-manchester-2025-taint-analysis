lazy val secureTypes = RootProject(uri("https://github.com/mdipirro/scala-secure-types.git"))

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .dependsOn(secureTypes)
  .settings(
    name := """play-scala-forms-example""",
    version := "1.0-SNAPSHOT",
    crossScalaVersions := Seq("3.7.3"),
    scalaVersion := crossScalaVersions.value.head,
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-Werror"
    ),
    // Needed for ssl-config to create self signed certificated under Java 17
    Test / javaOptions ++= List("--add-exports=java.base/sun.security.x509=ALL-UNNAMED"),
  )