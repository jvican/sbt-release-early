inScope(Scope.GlobalScope)(List(
  // Sonatype can only publish to this fake group id
  organization := "ch.epfl.scala",
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
  releaseEarlyWith := BintrayPublisher // this is the default
))

inThisBuild(List(
  // Bintray settings -- This one has to be redefined in `ThisBuild`
  bintrayOrganization := Some("scalaplatform")
))

lazy val publishSettings = Seq(
  publishMavenStyle := false,
  publishArtifact in Test := false,
  // Disable publishing of docs and sources
  publishArtifact in (Compile, packageDoc) := false,
  publishArtifact in (Compile, packageSrc) := false,
  resolvers += Resolver.jcenterRepo,
  resolvers += Resolver.bintrayRepo("scalaplatform", "tools"),
  updateOptions := updateOptions.value.withCachedResolution(true),
  // Bintray settings -- These ones have to be redefined in the projects
  bintrayRepository := "tools",
  bintrayPackageLabels := Seq("scala", "scalacenter", "plugin", "sbt")
)

// This is just necessary to test with scripted, don't copy
lazy val scriptedTest = Seq(releaseEarlyEnableLocalReleases := true)

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

val allowed = "0123456789abcdef"
val randomVersion =
  scala.util.Random.alphanumeric
    .filter((c: Char) => allowed.contains(c))
    .take(8).mkString("")
val randomizeVersion = taskKey[Unit]("Randomize version")
randomizeVersion in ThisBuild := {
  val currentVersion = version.in(ThisBuild).value
  val default = currentVersion.endsWith("-SNAPSHOT")
  import ch.epfl.scala.sbt.release.ReleaseEarly.Defaults
  if (Defaults.isDynVerSnapshot(dynverGitDescribeOutput.value, default))
    sys.error("Version has to be derived from a git tag.")

  val logger = state.value.log
  val newRandomVersion = s"v0.2.0+1-$randomVersion"
  logger.info(s"Adding random version to test git tag: $newRandomVersion")
  val process =
    sys.process.Process(s"""git tag -a $newRandomVersion -m hehe""",
                Option(baseDirectory.in(ThisBuild).value))
  assert(process.! == 0)
}
