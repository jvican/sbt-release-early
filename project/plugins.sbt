logLevel := Level.Warn
resolvers += Classpaths.sbtPluginReleases
resolvers += Resolver.bintrayIvyRepo("scalaplatform", "tools")

addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0-M15-5")

libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value

// Trick to use this plugin as its own plugin :)
unmanagedSourceDirectories in Compile +=
  baseDirectory.value.getParentFile / "src" / "main" / "scala"
addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")
addSbtPlugin("com.dwijnand" % "sbt-dynver" % "1.2.0")
