package ch.epfl.scala.sbt.release

object Feedback {
  private val prefix: String =
    s"${scala.Console.BLUE}sbt-release-early: ${scala.Console.RESET}"

  val OnlyCI = "The release task was not run inside the CI."
  val forceDefinitionOfScmInfo =
    "Missing `scmInfo`. Set it manually to generate correct POM files."
  val forceDefinitionOfDevelopers =
    "Missing `developers`. Set it manually to generate correct POM files."

  def skipRelease(projectName: String) =
    s"${prefix}Skip release for $projectName because `publishArtifact` is false."
  def logCheckRequirements(projectName: String) =
    s"${prefix}Checking requirements for $projectName."
  def logCheckSnapshots(projectName: String) =
    s"${prefix}Checking snapshots dependencies for $projectName."
  def logValidatePom(projectName: String) =
    s"${prefix}Validating POM files for $projectName."
  def logSyncToMaven(projectName: String) =
    s"${prefix}Syncing $projectName's artifacts to Maven Central."
  def logReleaseEarly(projectName: String) =
    s"${prefix}Executing release process for $projectName."

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

  private val bypassSnapshotSettingKey: String =
    ReleaseEarlyPlugin.autoImport.releaseEarlyBypassSnapshotCheck.key.label
  def detectedSnapshotsDependencies(deps: Seq[sbt.ModuleID]) =
    s"""
      |Aborting release process. Snapshot dependencies have been detected:
      |${deps.mkString("\t", "\n", "")}
      |
      |Releasing artifacts that depend on snapshots produce non-deterministic behaviour.
      |You can disable this check by enabling `$bypassSnapshotSettingKey`.
    """.stripMargin

  val fixRequirementErrors =
    s"${prefix}Found errors that need to be fixed before proceeding."
}
