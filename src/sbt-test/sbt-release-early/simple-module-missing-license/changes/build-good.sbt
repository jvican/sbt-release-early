name := "root"
organization := "me.vican.jorge"
scalaVersion := "2.12.2"

pgpPublicRing in Global := file("/drone/.gnupg/pubring.asc")
pgpSecretRing in Global := file("/drone/.gnupg/secring.asc")

homepage := Some(url("https://github.com/jvican/root-example"))
// The id of this license is incorrect
licenses := Seq("MPL-2.0" -> url("https://opensource.org/licenses/MPL-2.0"))
pomExtra in Global := {
  <developers>
    <developer>
      <id>jvican</id>
      <name>Jorge Vicente Cantero</name>
      <url>https://github.com/jvican</url>
    </developer>
  </developers>
  <scm>
    <developerConnection>scm:git:git@github.com:jvican</developerConnection>
    <url>https://github.com/jvican/root-example.git</url>
    <connection>scm:git:git@github.com:jvican/root-example.git</connection>
  </scm>
}

// Bintray
bintrayOrganization := None
bintrayRepository := "releases"
bintrayPackage := "root-example"
releaseEarlyWith := BintrayPublisher

// Disable publishing of docs and sources
publishArtifact in (Compile, packageDoc) := false
publishArtifact in (Compile, packageSrc) := false

// Release early
releaseEarlyEnableLocalReleases := true

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
