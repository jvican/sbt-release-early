name := "root"
organization := "me.vican.jorge"
scalaVersion := "2.12.2"

homepage := Some(url("https://github.com/jvican/root-example"))
licenses := Seq("MPL-2.0" -> url("https://opensource.org/licenses/MPL-2.0"))
pomExtra in Global := {
  <scm>
    <developerConnection>scm:git:git@github.com:jvican</developerConnection>
    <url>https://github.com/jvican/root-example.git</url>
    <connection>scm:git:git@github.com:jvican/root-example.git</connection>
  </scm>
  <developers>
    <developer>
      <id>jvican</id>
      <name>Jorge Vicente Cantero</name>
      <url>https://github.com/jvican</url>
    </developer>
  </developers>
}

// Bintray
bintrayOrganization := None
bintrayRepository := "releases"
bintrayPackage := "root-example"
publishTo := (publishTo in bintray).value
releaseEarlyWith := BintrayPublisher
releaseEarlyEnableInstantReleases := false

// Release early
releaseEarlyEnableLocalReleases := true

releaseEarlyCheckRequirements := {
  sys.error("`releaseEarlyCheckRequirements` should not be called, instant releases are disabled")
  releaseEarlyCheckRequirements.value
}
