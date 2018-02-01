name := "simple-module-missing-vcs"
organization := "me.vican.jorge"
scalaVersion := "2.12.2"

homepage := Some(url("https://github.com/jvican/root-example"))
licenses := Seq("MPL-2.0" -> url("https://opensource.org/licenses/MPL-2.0"))
pomExtra in Global := {
  // The scm information is missing
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

// Release early
releaseEarlyEnableLocalReleases := true
