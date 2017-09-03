lazy val `sbt-release-early` = project
  .in(file("."))
  .settings(ScriptedPlugin.scriptedSettings)
  .settings(
    sbtPlugin := true,
    pgpPublicRing := file("/drone/.gnupg/pubring.asc"),
    pgpSecretRing := file("/drone/.gnupg/secring.asc"),
    scalaVersion := "2.10.6",
    addSbtPlugin("ch.epfl.scala" % "sbt-bintray" % "0.5.0"),
    addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.1"),
    addSbtPlugin("com.dwijnand" % "sbt-dynver" % "1.3.0"),
    addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "1.1"),
    scriptedLaunchOpts := Seq(
      "-Dplugin.version=" + version.value,
      "-Xmx1g",
      "-Xss16m"
    ),
    scriptedBufferLog := false
  )
