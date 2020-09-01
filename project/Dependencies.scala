import sbt.Keys.libraryDependencies
import sbt._

object Dependencies {
  val cirisVersion = "1.0.4"
  val circeVersion = "0.13.0"
  val fs2AwsVersion = "3.0.2"
  val Http4sVersion = "0.21.6"
  val ScanamoVersion = "1.0.0-M12-1"

  val Config = Seq(
    libraryDependencies ++= Seq(
      "is.cir" %% "ciris-enumeratum" % cirisVersion,
      "is.cir" %% "ciris-refined" % cirisVersion,
      "eu.timepit" %% "refined" % "0.9.14",
      "com.ovoenergy" %% "ciris-aws-ssm" % "1.0.0"
    )
  )

  val Http4s = Seq(
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion withSources (),
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion withSources (),
      "org.http4s" %% "http4s-circe" % Http4sVersion withSources (),
      "org.http4s" %% "http4s-dsl" % Http4sVersion withSources (),
      "org.http4s" %% "http4s-client" % Http4sVersion withSources (),
      "org.http4s" %% "jawn-fs2" % "0.15.0" withSources ()
    )
  )

  val Circe = Seq(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-generic-extras" % "0.13.0",
      "io.circe" %% "circe-parser" % circeVersion,
      "io.circe" %% "circe-refined" % circeVersion,
      "io.circe" %% "circe-optics" % "0.13.0",
      "io.circe" %% "circe-fs2" % "0.13.0",
      "com.beachape" %% "enumeratum-circe" % "1.6.0"
    )
  )

  val fs2AWS = Seq(
    libraryDependencies ++= Seq(
      "io.laserdisc" %% "fs2-aws" % fs2AwsVersion,
      "io.laserdisc" %% "fs2-aws-s3" % fs2AwsVersion,
      "io.laserdisc" %% "fs2-aws-testkit" % fs2AwsVersion % Test
    )
  )

  val Logging = Seq(
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "ch.qos.logback" % "logback-core" % "1.2.3",
      "org.slf4j" % "jcl-over-slf4j" % "1.7.30",
      "org.slf4j" % "jul-to-slf4j" % "1.7.30",
      "io.chrisdavenport" %% "log4cats-slf4j" % "1.0.1"
    )
  )

  val TestLib = Seq(
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.1.1" % Test, // ApacheV2
      "org.scalamock" %% "scalamock" % "4.4.0" % Test,
      "org.mockito" %% "mockito-scala-scalatest" % "1.13.11" % Test,
      "org.mockito" %% "mockito-scala-cats" % "1.13.11" % Test,
      "org.mockito" % "mockito-core" % "3.3.3" % Test,
      "org.scalacheck" %% "scalacheck" % "1.14.3" % Test,
      "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % "1.2.5" % Test,
      "com.danielasfregola" %% "random-data-generator" % "2.8" % Test,
      "io.laserdisc" %% "fs2-aws-testkit" % fs2AwsVersion % Test,
      "com.dimafeng" %% "testcontainers-scala" % "0.36.1" % Test,
      "org.testcontainers" % "mockserver" % "1.14.1" % Test,
      "org.mock-server" % "mockserver-netty" % "5.9.0" % Test,
      "org.scanamo" %% "scanamo-testkit" % ScanamoVersion
    )
  )

  val XML = Seq(
    libraryDependencies ++= Seq(
      "javax.xml.bind" % "jaxb-api" % "2.3.1",
      "com.sun.xml.bind" % "jaxb-core" % "2.3.0.1",
      "com.sun.xml.bind" % "jaxb-impl" % "2.3.3",
      "javax.activation" % "activation" % "1.1.1"
    )
  )

  val Dynamo = Seq(
    libraryDependencies ++= Seq(
      "org.scanamo" %% "scanamo" % ScanamoVersion,
      "org.scanamo" %% "scanamo-refined" % ScanamoVersion,
      "org.scanamo" %% "scanamo-cats-effect" % ScanamoVersion,
      "io.laserdisc" %% "scanamo-circe" % "1.0.8"
    )
  )
}
