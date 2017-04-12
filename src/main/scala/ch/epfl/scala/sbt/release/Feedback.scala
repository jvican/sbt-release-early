package ch.epfl.scala.sbt.release

object Feedback {
  val OnlyCI = "The release task was not run inside the CI."
  val forceDefinitionOfScmInfo =
    "Missing `scmInfo`. Set it manually to generate correct POM files."
  val forceValidLicense = s"""
      |Maven Central requires your POM files to define a valid license.
      |Valid licenses are: ${bintry.Licenses.Names}.
    """.stripMargin
}
