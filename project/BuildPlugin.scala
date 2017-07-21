package build

import sbt.{AutoPlugin, Def, Keys, PluginTrigger, Plugins}

object BuildPlugin extends AutoPlugin {
  override def trigger: PluginTrigger = allRequirements
  override def requires: Plugins = ch.epfl.scala.sbt.release.ReleaseEarlyPlugin

  override def globalSettings: Seq[Def.Setting[_]] =
    BuildDefaults.globalSettings
  override def buildSettings: Seq[Def.Setting[_]] =
    BuildDefaults.buildSettings
  override def projectSettings: Seq[Def.Setting[_]] =
    BuildDefaults.projectSettings
}

object BuildDefaults {
  import sbt.{url, file, richFile}
  import sbt.{ScmInfo, Developer, Resolver, ThisBuild, Watched, Compile, Test}

  // This should be added to upstream sbt.
  def GitHub(org: String, project: String): java.net.URL =
    url(s"https://github.com/$org/$project")
  def GitHubDev(handle: String, fullName: String, email: String) =
    Developer(handle, fullName, email, url(s"https://github.com/$handle"))

  import com.typesafe.sbt.SbtPgp.{autoImport => PgpKeys}
  import ch.epfl.scala.sbt.release.ReleaseEarlyPlugin.{autoImport => ReleaseEarlyKeys}

  private final val ThisRepo = GitHub("scalacenter", "sbt-release-early")
  final val globalPublishSettings: Seq[Def.Setting[_]] = Seq(
    // Global settings to set up repository-related settings
    Keys.licenses := Seq(
      "MPL-2.0" -> url("http://opensource.org/licenses/MPL-2.0")),
    Keys.homepage := Some(ThisRepo),
    Keys.autoAPIMappings := true,
    Keys.scmInfo := Some(
      ScmInfo(ThisRepo,
              "scm:git:git@github.com:scalacenter/sbt-release-early.git")),
    Keys.developers := List(
      GitHubDev("jvican", "Jorge Vicente Cantero", "jorge@vican.me")),
    // Necessary to publish for our Drone CI -- specific to this repo setup.
    PgpKeys.pgpPublicRing := file("/drone/.gnupg/pubring.asc"),
    PgpKeys.pgpSecretRing := file("/drone/.gnupg/secring.asc"),
    ReleaseEarlyKeys.releaseEarlyWith := ReleaseEarlyKeys.SonatypePublisher
  )

  final val globalSettings: Seq[Def.Setting[_]] = Seq(
    Keys.triggeredMessage in ThisBuild := Watched.clearWhenTriggered,
    Keys.watchSources += (Keys.baseDirectory in ThisBuild).value / "resources",
    Keys.testOptions in Test += sbt.Tests.Argument("-oD")
  ) ++ globalPublishSettings

  final val buildSettings: Seq[Def.Setting[_]] = Seq(
    Keys.organization := "ch.epfl.scala",
    Keys.resolvers += Resolver.jcenterRepo,
    Keys.updateOptions := Keys.updateOptions.value.withCachedResolution(true)
  )

  final val projectSettings: Seq[Def.Setting[_]] = Seq(
    Keys.scalacOptions in Compile := reasonableCompileOptions
  )

  final val reasonableCompileOptions = (
    "-deprecation" :: "-encoding" :: "UTF-8" :: "-feature" :: "-language:existentials" ::
      "-language:higherKinds" :: "-language:implicitConversions" :: "-unchecked" ::
      "-Yno-adapted-args" :: "-Ywarn-numeric-widen" :: "-Xfuture" :: "-Xlint" :: Nil
  )
}
