import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.banking",
      scalaVersion := "2.12.4",
      version      := "0.0.1-SNAPSHOT"
    )),
    name := "Bank",
    libraryDependencies += scalaTest % Test,

    libraryDependencies ++= {
      val akkaV = "2.4.17"
      val akkaHttpV = "10.0.9"
      val slickV = "3.2.0"
      Seq(
        "com.typesafe.akka" %% "akka-http" % akkaHttpV,
        "com.typesafe.akka" %% "akka-http-core" % akkaHttpV,
        "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % Test,
        "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
        "com.typesafe.akka" %% "akka-actor" % akkaV,
        "com.byteslounge" %% "slick-repo" % "1.4.3",
        "org.scalatest" %% "scalatest" % "3.0.4",
        "org.scalamock" %% "scalamock" % "4.1.0" % Test,
        "com.typesafe.slick" %% "slick" % slickV,
        "com.typesafe.slick" %% "slick-hikaricp" % slickV,
        "com.h2database" % "h2" % "1.3.175",
        "ch.qos.logback" % "logback-classic" % "1.2.3"
      )
    }
  )
