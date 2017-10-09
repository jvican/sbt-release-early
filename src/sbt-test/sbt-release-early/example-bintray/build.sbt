lazy val publishSettings = Seq(
  publishMavenStyle := false,
  bintrayOrganization := Some("scalaplatform"),
  bintrayRepository := "tools",
  bintrayPackageLabels := Seq("scala", "scalacenter", "plugin", "sbt"),
  publishTo := (publishTo in bintray).value,
  releaseEarlyWith := BintrayPublisher,
  publishArtifact in Test := false,
  licenses := Seq(
    // Scala Center license... BSD 3-clause
    "BSD" -> url("http://opensource.org/licenses/BSD-3-Clause")
  ),
  homepage := Some(url("https://github.com/scalacenter/sbt-release-early")),
  autoAPIMappings := true,
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/scalacenter/sbt-release-early"),
      "scm:git:git@github.com:scalacenter/sbt-release-early.git"
    )),
  developers := List(
    Developer("jvican",
              "Jorge Vicente Cantero",
              "jorge@vican.me",
              url("https://github.com/jvican"))
  )
)

// This is just necessary to test with scripted, don't copy
lazy val scriptedTest = Seq(
  releaseEarlyEnableLocalReleases := true
)

lazy val buildSettings = Seq(
  organization := "ch.epfl.scala",
  resolvers += Resolver.jcenterRepo,
  resolvers += Resolver.bintrayRepo("scalaplatform", "tools"),
  updateOptions := updateOptions.value.withCachedResolution(true)
)

lazy val requiredSettings: Seq[Setting[_]] =
  publishSettings ++ buildSettings

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

lazy val root = project
  .in(file("."))
  .settings(noPublish)
  .settings(buildSettings)
  .settings(scriptedTest)
  .aggregate(p1, p2)

lazy val p1 = project
  .in(file("p1"))
  .settings(requiredSettings)
  .settings(scriptedTest)
  .settings(scalaVersion := "2.11.8")

lazy val p2 = project
  .in(file("p2"))
  .settings(scriptedTest)
  .settings(requiredSettings)
  .settings(scalaVersion := "2.11.8")

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
    sbt.Process(s"""git tag -a $newRandomVersion -m hehe""",
                Option(baseDirectory.in(ThisBuild).value))
  assert(process.! == 0)
}
