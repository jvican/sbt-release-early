lazy val `sbt-release-early` = project
  .in(file("."))
  .settings(
    sbtPlugin := true,
    scalaVersion := "2.12.4",
    sbtVersion in Global := "1.1.1",
    addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.3"),
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
