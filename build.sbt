useGpg in Global := true
lazy val `sbt-release-early` = project
  .enablePlugins(ScriptedPlugin)
  .in(file("."))
  .settings(
    sbtPlugin := true,
    scalaVersion := "2.12.10",
    addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.0.0"),
    addSbtPlugin("com.dwijnand" % "sbt-dynver" % "4.0.0"),
    addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.8.1"),
    addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.6"),
    scriptedLaunchOpts := Seq(
      "-Dplugin.version=" + version.value,
      "-Xmx1g",
      "-Xss16m"
    ),
    scriptedBufferLog := false
  )
