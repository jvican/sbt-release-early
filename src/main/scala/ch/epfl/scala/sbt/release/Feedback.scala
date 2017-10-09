package ch.epfl.scala.sbt.release

import bintray.BintrayPlugin
import com.typesafe.sbt.pgp.PgpKeys
import sbt.Keys

object Feedback {
  private val prefix: String =
    s"${scala.Console.BLUE}sbt-release-early: ${scala.Console.RESET}"
  private def bold(toEmbolden: String): String =
    s"${scala.Console.BOLD}$toEmbolden${scala.Console.RESET}"

  val OnlyCI = "The release task was not run inside the CI."

  def skipRelease(projectName: String) =
    s"${prefix}Skip release for $projectName because `publishArtifact` is false."
  def skipBintrayCredentialsCheck(projectName: String) =
    s"${prefix}Skip check of bintray credentials for $projectName because underlying publisher is not bintray."
  def skipSonatypeCredentialsCheck(projectName: String) =
    s"${prefix}Skip check of sonatype credentials for $projectName."
  def skipInstantRelease(projectName: String, version: String) =
    s"${prefix}Skip instant release of $projectName for version $version. Instant releases are disabled."
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
  def logReleaseSonatype(projectName: String) =
    s"${prefix}Releasing $projectName to Sonatype."
  def logReleaseBintray(projectName: String) =
    s"${prefix}Releasing $projectName to Bintray."

  import ReleaseEarlyPlugin.autoImport.{releaseEarlyNoGpg, releaseEarlyEnableSyncToMaven}
  val BintrayInconsistentGpgState =
    s"""${prefix}Inconsistent configuration breaks bintray releases of stable versions:
       |  1. `${releaseEarlyNoGpg.key.label} := true` and
       |  2. `${releaseEarlyEnableSyncToMaven.key.label} := true`
       |
       |If you ignore gpg completely, you cannot release to Maven Central.
     """.stripMargin

  val SonatypeInconsistentGpgState =
    s"""${prefix}Inconsistent configuration breaks any sonatype release.
      |
      |When Sonatype is used as the publisher, `${releaseEarlyNoGpg.key.label}` cannot be
      |set to false because Maven Central requires signed releases.
    """.stripMargin

  val LogFetchPgpCredentials =
    s"${prefix}Trying to fetch `${PgpKeys.pgpPassphrase.key.label} in Global` from the environment."
  val LogAddSonatypeCredentials =
    s"${prefix}Adding sonatype credentials to `${Keys.credentials.key.label}` caught from the environment."

  val forceValidLicense = s"""
      |Maven Central requires your POM files to use a valid license id.
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
    s"""
      |Sonatype credentials are missing. Make sure that:
      |  1. System properties (sona.user and sona.pass) are available; or
      |  2. Environment variables (SONA_USER and SONA_PASS) are set.
      |
      |Otherwise they cannot be fetched programmatically.
    """.stripMargin.trim

  val RecommendedScope: String =
    s"The recommended scope for either of these keys is ${bold("`ThisBuild`")}."

  val missingVcsUrl =
    s"""
      |The vcs url information is missing. Make sure that:
      |  * The key `${Keys.scmInfo.key.label}` is defined and correctly scoped; or
      |  * The key `${Keys.pomExtra.key.label}` does contain the scm url xml node; or
      |  * The key `${BintrayPlugin.autoImport.bintrayVcsUrl.key.label}` is defined and correctly scoped.
      |
      |Use `inspect` to check the scopes of your current definitions.
      |$RecommendedScope
    """.stripMargin

  val missingDevelopers =
    s"""
      |The developers information is missing. Make sure that:
      |  * The key `${Keys.developers.key}` is defined; or
      |  * The key `${Keys.pomExtra.key}` contains the `developers` xml node.
      |
      |Use `inspect` to check the scopes of your current definitions.
      |$RecommendedScope
    """.stripMargin

  import ReleaseEarlyPlugin.autoImport.releaseEarlyWith
  val UnrecognisedPublisher: String =
    s"{$prefix}The publisher backend selected in `${releaseEarlyWith.key.label}` is unrecognised."
  def unsupportedSnapshot(version: String): String =
    s"{$prefix}Detected snapshot version: $version. SNAPSHOTs are not supported."

  def skipSyncToMaven(projectName: String) =
    s"${prefix}Skipping Maven Central synchronization for $projectName."

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
