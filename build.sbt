lazy val `sbt-release-early` = project
  .in(file("."))
  .settings(ScriptedPlugin.scriptedSettings)
  .settings(
    sbtPlugin := true,
    scalaVersion := "2.10.6",
    addSbtPlugin("ch.epfl.scala" % "sbt-bintray" % "0.5.0"),
    addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0"),
    addSbtPlugin("com.dwijnand" % "sbt-dynver" % "1.3.0"),
    addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "1.1"),
    scriptedLaunchOpts := Seq(
      "-Dplugin.version=" + version.value,
      "-Xmx1g",
      "-Xss16m"
    ),
    scriptedBufferLog := false,
    fork in Test := true
  )
