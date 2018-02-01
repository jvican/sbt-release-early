inScope(Scope.GlobalScope)(List(
  // Sonatype can only publish to this fake group id
  organization := "org.bitbucket.jplantdev",
  licenses := Seq("BSD" -> url("http://opensource.org/licenses/BSD-3-Clause")),
  homepage := Some(url("https://github.com/$ORG/$PROJECT")),
  developers := List(Developer("jvican", "Jorge Vicente Cantero", "jorge@vican.me", url("https://github.com/jvican"))),
  // This is only necessary because scripted tests do not have any remote
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/scalacenter/sbt-release-early"),
      "scm:git:git@github.com:scalacenter/sbt-release-early.git"
  )),
  // These are the sbt-release-early settings to configure
  pgpPublicRing := file("/drone/.gnupg/pubring.asc"),
  pgpSecretRing := file("/drone/.gnupg/secring.asc"),
  releaseEarlyWith := SonatypePublisher // this is the default
))

inThisBuild(List(
  version in ThisBuild := "0.2.0+1-30919445",
  dynver in ThisBuild := "0.2.0+1-30919445"
))

lazy val publishSettings = Seq(
  publishArtifact in Test := false,
  resolvers += Resolver.jcenterRepo,
  resolvers += Resolver.bintrayRepo("scalaplatform", "tools"),
  updateOptions := updateOptions.value.withCachedResolution(true)
)

val dontCallMe = taskKey[Unit]("Don't call me, I'll fail.")
lazy val scriptedTest = Seq(
  // This is just necessary to test with scripted, don't copy
  releaseEarlyEnableLocalReleases := true,
  dontCallMe := sys.error("Release early process shoult not be invoked for repeated release."),
  releaseEarlyProcess := List(dontCallMe)
)

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

lazy val root = project
  .in(file("."))
  .settings(noPublish)
  .settings(scriptedTest)
  .aggregate(p1, p2)

lazy val p1 = project
  .in(file("p1"))
  .settings(publishSettings)
  .settings(scriptedTest)
  .settings(scalaVersion := "2.11.11")

lazy val p2 = project
  .in(file("p2"))
  .settings(scriptedTest)
  .settings(publishSettings)
  .settings(scalaVersion := "2.11.11")
