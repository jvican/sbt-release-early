logLevel := Level.Warn
resolvers += Classpaths.sbtPluginReleases
resolvers += Resolver.bintrayIvyRepo("scalaplatform", "tools")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value

// This project is its own plugin :)
unmanagedSourceDirectories in Compile +=
  baseDirectory.value.getParentFile / "src" / "main" / "scala"

// This is required only for the recursive plugin dependency
// Users of this plugin don't need to add this to their plugins.sbt
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.3")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.0.0")
addSbtPlugin("com.dwijnand" % "sbt-dynver" % "2.0.0")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.0")
