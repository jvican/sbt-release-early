name := "simple-module"
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
releaseEarlyWith := BintrayPublisher

// Disable publishing of docs and sources
publishArtifact in (Compile, packageDoc) := false
publishArtifact in (Compile, packageSrc) := false

// Release early
releaseEarlyEnableLocalReleases := true
