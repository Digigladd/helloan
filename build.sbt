organization in ThisBuild := "com.digigladd"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.4"

lazy val `helloan` = (project in file("."))
  .aggregate(`helloan-sync-api`, `helloan-sync-impl`, `helloan-utils`, `helloan-publication-api`, `helloan-publication-impl`, `helloan-seance-api`, `helloan-seance-impl`)

lazy val `helloan-sync-api` = (project in file("helloan-sync-api"))
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lagomJavadslJackson,
      lombok
    )
  )
  .dependsOn(`helloan-utils`)

lazy val `helloan-sync-impl` = (project in file("helloan-sync-impl"))
  .enablePlugins(LagomJava, JavaAppPackaging)
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslKafkaBroker,
      lagomLogback,
      lagomJavadslTestKit,
      lombok,
      akkaHttp,
      akkaStream,
      apacheCommonsIO
    )
  )
  .settings(
    dockerExposedPorts := Seq(9000),
    version in Docker := "1.0",
    packageName in Docker := "digigladd/helloan/sync"
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`helloan-utils`,`helloan-sync-api`)

lazy val `helloan-utils` = (project in file("helloan-utils"))
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomLogback,
      lagomJavadslApi,
      apacheCommonsIO,
      apacheCommonsCompress,
      lombok
    )
  )

lazy val `helloan-publication-api` = (project in file("helloan-publication-api"))
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )

lazy val `helloan-publication-impl` = (project in file("helloan-publication-impl"))
  .enablePlugins(LagomJava, JavaAppPackaging)
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslKafkaBroker,
      lagomLogback,
      lagomJavadslTestKit,
      lombok,
      cassandraExtras
    )
  )
  .settings(
    dockerExposedPorts := Seq(9000),
    version in Docker := "1.0",
    packageName in Docker := "digigladd/helloan/publication"
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`helloan-publication-api`,`helloan-sync-api`,`helloan-seance-api`,`helloan-utils`)

lazy val `helloan-seance-api` = (project in file("helloan-seance-api"))
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )

lazy val `helloan-seance-impl` = (project in file("helloan-seance-impl"))
  .enablePlugins(LagomJava, JavaAppPackaging)
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslKafkaBroker,
      lagomLogback,
      lagomJavadslTestKit,
      lombok,
      cassandraExtras
    )
  )
  .settings(
    dockerExposedPorts := Seq(9000),
    version in Docker := "1.0",
    packageName in Docker := "digigladd/helloan/seance"
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`helloan-seance-api`,`helloan-utils`)

val lombok = "org.projectlombok" % "lombok" % "1.16.18"
val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.1.5"
val akkaStream = "com.typesafe.akka" %% "akka-stream" % "2.5.16"
val apacheCommonsIO = "commons-io" % "commons-io" % "2.6"
val apacheCommonsCompress = "org.apache.commons" % "commons-compress" % "1.18"
val cassandraExtras = "com.datastax.cassandra" % "cassandra-driver-extras" % "3.6.0"

def common = Seq(
  javacOptions in compile += "-parameters"
)

lagomUnmanagedServices in ThisBuild += ("elasticearch" -> "http://elasticsearch:9200")