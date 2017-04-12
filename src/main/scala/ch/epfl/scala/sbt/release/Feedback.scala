package ch.epfl.scala.sbt.release

object Feedback {
  val OnlyCI = "The release task was not run inside the CI."
  val forceDefinitionOfScmInfo =
    "Missing `scmInfo`. Set it manually to generate correct POM files."
  val forceDefinitionOfDevelopers =
    "Missing `developers`. Set it manually to generate correct POM files."

  val forceValidLicense = s"""
      |Maven Central requires your POM files to define a valid license.
      |Valid licenses are: ${bintry.Licenses.Names}.
    """.stripMargin.trim

  val missingBintrayCredentials =
    """
      |Bintray credentials are missing. Aborting.
      |Make sure that:
      |  1. The bintray credentials file exists as required by `sbt-bintray`.
      |  2. The value of `bintrayCredentialsFile` points to the correct file.
    """.stripMargin.trim

  val missingSonatypeCredentials =
    """
      |Sonatype credentials are missing. Aborting.
      |Make sure that the credentials are available via:
      |  1. System properties (sona.user and sona.pass).
      |  2. Environment variables (SONA_USER and SONA_PASS).
      |
      |Otherwise they cannot be fetched programmatically.
    """.stripMargin.trim

  val fixRequirementErrors =
    "Found errors that need to be fixed before proceeding."
}
