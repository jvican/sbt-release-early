logLevel := Level.Warn
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")

{
  val pluginVersion = System.getProperty("plugin.version")
  if(pluginVersion == null)
    throw new RuntimeException("""|The system property 'plugin.version' is not defined.
                                  |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
  else addSbtPlugin("ch.epfl.scala" % "sbt-release-early" % pluginVersion)
}
