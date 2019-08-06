val mainScala = "2.12.8"
val allScala  = Seq("2.11.12", mainScala)

organization := "dev.zio"
homepage := Some(url("https://github.com/zio/zio-akka-cluster"))
name := "zio-akka-cluster"
licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
scalaVersion := mainScala
parallelExecution in Test := false
fork in Test := true
pgpPublicRing := file("/tmp/public.asc")
pgpSecretRing := file("/tmp/secret.asc")
releaseEarlyWith := SonatypePublisher
scmInfo := Some(
  ScmInfo(url("https://github.com/zio/zio-akka-cluster/"), "scm:git:git@github.com:zio/zio-akka-cluster.git")
)
developers := List(
  Developer(
    "ghostdogpr",
    "Pierre Ricadat",
    "ghostdogpr@gmail.com",
    url("https://github.com/ghostdogpr")
  )
)

lazy val zioOrg       = "dev.zio"
lazy val zioVersion   = "1.0.0-RC10-1"

lazy val zio          = zioOrg %% "zio"         % zioVersion
lazy val zioStreams   = zioOrg %% "zio-streams" % zioVersion

lazy val akkaOrg      = "com.typesafe.akka"
lazy val akkaVersion  = "2.5.23"

lazy val scalaTest    = "org.scalatest"     %% "scalatest"             % "3.0.8" % "test"


libraryDependencies ++= Seq(zio,zioStreams, scalaTest,
  akkaOrg %% "akka-cluster-tools"    % akkaVersion,
  akkaOrg %% "akka-cluster-sharding" % akkaVersion,
  compilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3"),
  compilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"))


lazy val clusterTyped : Project = Project(id="ClusterTyped", base =file("zio-akka-cluster-typed"))
  .settings(
    name:= "zioAkkaClusterTyped",
    libraryDependencies++=Seq(zio,zioStreams, scalaTest,
      akkaOrg %% "akka-cluster-typed" % akkaVersion,
      akkaOrg %% "akka-cluster-sharding-typed" % akkaVersion,
      akkaOrg %% "akka-persistence-typed" % akkaVersion))

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-explaintypes",
  "-Yrangepos",
  "-feature",
  "-Xfuture",
  "-language:higherKinds",
  "-language:existentials",
  "-unchecked",
  "-Xlint:_,-type-parameter-shadow",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Ywarn-value-discard"
) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
  case Some((2, 11)) =>
    Seq(
      "-Yno-adapted-args",
      "-Ypartial-unification",
      "-Ywarn-inaccessible",
      "-Ywarn-infer-any",
      "-Ywarn-nullary-override",
      "-Ywarn-nullary-unit"
    )
  case Some((2, 12)) =>
    Seq(
      "-Xsource:2.13",
      "-Yno-adapted-args",
      "-Ypartial-unification",
      "-Ywarn-extra-implicit",
      "-Ywarn-inaccessible",
      "-Ywarn-infer-any",
      "-Ywarn-nullary-override",
      "-Ywarn-nullary-unit",
      "-opt-inline-from:<source>",
      "-opt-warnings",
      "-opt:l:inline"
    )
  case _ => Nil
})

fork in run := true




crossScalaVersions := allScala

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
