name := "root"
organization := "me.vican.jorge"
scalaVersion := "2.12.2"

homepage := Some(url("https://github.com/jvican/root-example"))
// The id of this license is incorrect
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
publishTo := (publishTo in bintray).value

// Release early
releaseEarlyEnableLocalReleases := true
