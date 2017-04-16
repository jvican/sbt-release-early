lazy val publishSettings = Seq(
  publishMavenStyle := false,
  bintrayOrganization := Some("scalaplatform"),
  bintrayRepository := "tools",
  bintrayPackageLabels := Seq("scala", "scalacenter", "plugin", "sbt"),
  publishTo := (publishTo in bintray).value,
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

def randomizeVersion(version: String): String =
  s"$version+${scala.math.abs(scala.util.Random.nextInt())}"

lazy val root = project
  .in(file("."))
  .settings(noPublish)
  .settings(scriptedTest)
  .dependsOn(p1, p2)

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

/* These are utilities for scripted to check git tags. */

version in ThisBuild := (Def.settingDyn {
  val currentVersion = (version in ThisBuild).value
  val default = currentVersion.endsWith("-SNAPSHOT")
  import ch.epfl.scala.sbt.release.ReleaseEarly.Defaults
  if (Defaults.isDynVerSnapshot(dynverGitDescribeOutput.value, default))
    Def.setting(sys.error("Version has to be derived from a git tag."))
  else {
    Def.setting {
      sLog.value.info(
        s"Randomizing version from tag $currentVersion to test publish.")
      randomizeVersion(currentVersion)
    }
  }
}).value

dynver in ThisBuild := (version in ThisBuild).value
