addCommandAlias("format", ";scalafmt;test:scalafmt;scalafmtSbt")
addCommandAlias("checkFormat", ";scalafmtCheck;test:scalafmtCheck;scalafmtSbtCheck")
addCommandAlias("build", ";checkFormat;clean;compile;test")
addCommandAlias(
  "validate",
  Seq("clean", "coverage", "test", "coverageReport", "coverageAggregate", "coverageOff").mkString(";", ";", "")
)

lazy val commonSettings = Seq(
  ThisBuild / turbo := true,
  fork in Test := true,
  scalaVersion := "2.13.2",
  sources in (Compile, doc) := Seq(),
  addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
  resolvers += Resolver.bintrayRepo("ovotech", "maven"),
  scalacOptions ++= Seq(
    "-encoding",
    "UTF-8", // source files are in UTF-8
    "-deprecation", // warn about use of deprecated APIs
    "-unchecked", // warn about unchecked type parameters
    "-feature", // warn about misused language features
    "-language:higherKinds", // allow higher kinded types without `import scala.language.higherKinds`
    "-language:implicitConversions", // allow use of implicit conversions
    "-language:postfixOps",
    "-Xlint", // enable handy linter warnings
    "-Xfatal-warnings", // turn compiler warnings into errors
    "-Ywarn-macros:after" // allows the compiler to resolve implicit imports being flagged as unused
  ),
  concurrentRestrictions in Global += Tags.limitAll(1)
)

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

lazy val root = project
  .in(file("."))
  .settings(commonSettings)
  .settings(noPublishSettings)
  .aggregate(
    `tc-demo-service`
  )

lazy val `tc-demo-service` = (project in file("tc-demo-service"))
  .settings(
    name := "datos-service",
    commonSettings,
    Dependencies.Circe,
    Dependencies.fs2AWS,
    Dependencies.Config,
    Dependencies.TestLib,
    Dependencies.Logging,
    Dependencies.Http4s,
    Dependencies.Dynamo
  )
