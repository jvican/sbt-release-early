lazy val `sbt-release-early` = project
  .in(file("."))
  .settings(
    sbtPlugin := true,
    pgpPublicRing := file("/drone/.gnupg/pubring.asc"),
    pgpSecretRing := file("/drone/.gnupg/secring.asc"),
    scalaVersion := "2.12.3",
    sbtVersion in Global := "1.0.2",
    addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.1+14-2cd81957"),
    addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0"),
    addSbtPlugin("com.dwijnand" % "sbt-dynver" % "2.0.0"),
    addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.0"),
    scriptedLaunchOpts := Seq(
      "-Dplugin.version=" + version.value,
      "-Xmx1g",
      "-Xss16m"
    ),
    scriptedBufferLog := false
  )
