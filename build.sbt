lazy val `sbt-release-early` = project
  .in(file("."))
  .enablePlugins(ScriptedPlugin)
  .settings(
    sbtPlugin := true,
    pgpPublicRing := file("/drone/.gnupg/pubring.asc"),
    pgpSecretRing := file("/drone/.gnupg/secring.asc"),
    scalaVersion := "2.12.10",
    addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.3"),
    addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.0.0"),
    addSbtPlugin("com.dwijnand" % "sbt-dynver" % "2.0.0"),
    addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.8"),
    scriptedLaunchOpts := Seq(
      "-Dplugin.version=" + version.value,
      "-Xmx1g",
      "-Xss16m"
    ),
    scriptedBufferLog := false
  )
