logLevel := Level.Warn
resolvers += Classpaths.sbtPluginReleases
resolvers += Resolver.bintrayIvyRepo("scalaplatform", "tools")

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0-M15-5")

libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value
