logLevel := Level.Warn
resolvers += Classpaths.sbtPluginReleases
resolvers += Resolver.bintrayIvyRepo("scalaplatform", "tools")

libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value

// This project is its own plugin :)
unmanagedSourceDirectories in Compile +=
  baseDirectory.value.getParentFile / "src" / "main" / "scala"

// This is required only for the recursive plugin dependency
// Users of this plugin don't need to add this to their plugins.sbt
addSbtPlugin("ch.epfl.scala" % "sbt-bintray" % "0.5.0")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.1")
addSbtPlugin("com.dwijnand" % "sbt-dynver" % "1.3.0")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "1.1")
