package ch.epfl.scala.sbt.release

import sbt.{AutoPlugin, Def, PluginTrigger, Plugins, Setting, Task}

object ReleaseEarlyPlugin extends AutoPlugin {
  object autoImport
      extends ReleaseEarlyKeys.ReleaseEarlySettings
      with ReleaseEarlyKeys.ReleaseEarlyTasks

  override def trigger: PluginTrigger = allRequirements
  override def requires: Plugins =
    sbtdynver.DynVerPlugin && bintray.BintrayPlugin && xerial.sbt.Sonatype

  override def projectSettings: Seq[Def.Setting[_]] =
    ReleaseEarly.projectSettings
  override def buildSettings: Seq[Def.Setting[_]] =
    ReleaseEarly.buildSettings
}

object ReleaseEarlyKeys {
  import sbt.{taskKey, settingKey, TaskKey, SettingKey}

  trait ReleaseEarlySettings {
    trait UnderlyingPublisher
    case object BintrayPublisher extends UnderlyingPublisher
    case object SonatypePublisher extends UnderlyingPublisher

    val releaseEarlyEnableLocalReleases: SettingKey[Boolean] =
      settingKey("Enable local releases.")
    val releaseEarlyInsideCI: SettingKey[Boolean] =
      settingKey("Detect whether sbt is running inside the CI.")
    val releaseEarlyBypassSnapshotCheck: SettingKey[Boolean] =
      settingKey("Bypass snapshots check, not failing if snapshots are found.")
    val releaseEarlyProcess: SettingKey[Seq[TaskKey[Unit]]] =
      settingKey("Release process executed by `releaseEarly`.")
    val releaseEarlyWith: SettingKey[UnderlyingPublisher] =
      settingKey("Specify the publisher to publish your artifacts.")
  }

  trait ReleaseEarlyTasks {
    val releaseEarly: TaskKey[Unit] =
      taskKey("Release early, release often.")
    val releaseEarlyValidatePom: TaskKey[Unit] =
      taskKey("Validate the data to generate a POM file.")
    val releaseEarlySyncToMaven: TaskKey[Unit] =
      taskKey("Synchronize to Maven Central.")
    val releaseEarlyEnableSyncToMaven: SettingKey[Boolean] =
      settingKey("Enable synchronization to Maven Central for git tags.")
    val releaseEarlyCheckRequirements: TaskKey[Unit] =
      taskKey("Check the requirements of the environment.")
    val releaseEarlyCheckSnapshotDependencies: TaskKey[Unit] =
      taskKey("Check snapshot dependencies before the release.")
    val releaseEarlyPublish: TaskKey[Unit] =
      taskKey(s"Publish artifact. Defaults to ${sbt.Keys.publish.key.label}.")
    val releaseEarlyClose: TaskKey[Unit] =
      taskKey("Materializes the release by closing staging repositories.")
  }
}

object ReleaseEarly {
  import sbt.{Keys, SettingKey}

  import ReleaseEarlyPlugin.autoImport._
  import xerial.sbt.Sonatype.{SonatypeCommand => Sonatype}
  import bintray.BintrayPlugin.{autoImport => Bintray}
  import sbtdynver.DynVerPlugin.{autoImport => DynVer}
  import com.typesafe.sbt.SbtPgp.{autoImport => Pgp}

  val buildSettings: Seq[Setting[_]] = Seq(
    Keys.isSnapshot := Defaults.isSnapshot.value
  )

  object PrivateKeys {
    // Note: code assumes that if it's not sonatype, it's Bintray.
    val releaseEarlyIsSonatype: SettingKey[Boolean] = sbt.settingKey("Caca")
  }

  val projectSettings: Seq[Setting[_]] = Seq(
    Keys.isSnapshot := Defaults.isSnapshot.value,
    releaseEarly := Defaults.releaseEarly.value,
    releaseEarlyWith := Defaults.releaseEarlyWith.value,
    releaseEarlyInsideCI := Defaults.releaseEarlyInsideCI.value,
    releaseEarlyEnableLocalReleases := Defaults.releaseEarlyEnableLocalReleases.value,
    releaseEarlySyncToMaven := Defaults.releaseEarlySyncToMaven.value,
    releaseEarlyEnableSyncToMaven := Defaults.releaseEarlyEnableSyncToMaven.value,
    releaseEarlyValidatePom := Defaults.releaseEarlyValidatePom.value,
    releaseEarlyCheckRequirements := Defaults.releaseEarlyCheckRequirements.value,
    releaseEarlyBypassSnapshotCheck := Defaults.releaseEarlyBypassSnapshotChecks.value,
    releaseEarlyCheckSnapshotDependencies := Defaults.releaseEarlyCheckSnapshotDependencies.value,
    releaseEarlyPublish := Defaults.releaseEarlyPublish.value,
    releaseEarlyClose := Defaults.releaseEarlyClose.value,
    releaseEarlyProcess := Defaults.releaseEarlyProcess.value,
    PrivateKeys.releaseEarlyIsSonatype := Defaults.releaseEarlyIsSonatype.value
  ) ++ Defaults.saneDefaults

  object Defaults extends Helper {
    import ReleaseEarlyPlugin.{autoImport => ThisPluginKeys}

    /* Sbt bug: `Def.sequential` here produces 'Illegal dynamic reference' when
     * used inside `Def.taskDyn`. This is reported upstream, unclear if it can be fixed. */
    private val StableDef = new sbt.TaskSequential {}

    // Currently unused, but stays here for future features
    val dynVer: Def.Initialize[String] = Def.setting {
      import sbtdynver.{DynVer => OriginalDynVer}
      val customVersion = DynVer.dynverGitDescribeOutput.value.map { info =>
        // Use '+' for the distance because it is semver compatible
        val commitPart = info.commitSuffix.mkString("+", "+", "")
        info.ref.dropV.value + commitPart + info.dirtySuffix.value
      }
      customVersion.getOrElse(
        OriginalDynVer.fallback(DynVer.dynverCurrentDate.value))
    }

    // See https://github.com/dwijnand/sbt-dynver/issues/23.
    val isSnapshot: Def.Initialize[Boolean] = Def.setting {
      isDynVerSnapshot(DynVer.dynverGitDescribeOutput.value,
                       Keys.isSnapshot.value)
    }

    val releaseEarlyEnableLocalReleases: Def.Initialize[Boolean] =
      Def.setting(false)

    val releaseEarlyWith: Def.Initialize[UnderlyingPublisher] =
      Def.setting(BintrayPublisher)

    val releaseEarlyInsideCI: Def.Initialize[Boolean] =
      Def.setting(sys.env.get("CI").isDefined)

    val releaseEarlyBypassSnapshotChecks: Def.Initialize[Boolean] =
      Def.setting(false)

    val releaseEarlyCheckSnapshotDependencies: Def.Initialize[Task[Unit]] = {
      Def.taskDyn {
        if (!ThisPluginKeys.releaseEarlyBypassSnapshotCheck.value) {
          val logger = Keys.streams.value.log
          logger.info(Feedback.logCheckSnapshots(Keys.name.value))
          val managedClasspath = (Keys.managedClasspath in sbt.Runtime).value
          val moduleIds = managedClasspath.flatMap(_.get(Keys.moduleID.key))
          // NOTE that we don't use sbt-release-early snapshot definition here.
          val snapshots = moduleIds.filter(m =>
            m.isChanging || m.revision.endsWith("-SNAPSHOT"))
          if (snapshots.nonEmpty)
            sys.error(Feedback.detectedSnapshotsDependencies(snapshots))
          else Def.task(())
        } else Def.task(())
      }
    }

    val releaseEarlyIsSonatype: Def.Initialize[Boolean] = Def.setting {
      val underlyingPublisher = ThisPluginKeys.releaseEarlyWith.value
      underlyingPublisher == SonatypePublisher
    }

    val releaseEarlyPublish: Def.Initialize[Task[Unit]] = Def.taskDyn {
      // If sonatype, always use `publishSigned` and ignore `publish`
      if (PrivateKeys.releaseEarlyIsSonatype.value) Pgp.PgpKeys.publishSigned
      // If it's not sonatype, it's bintray... use signed for stable releases
      else if (!Keys.isSnapshot.value) Pgp.PgpKeys.publishSigned
      // Else, non-stable releases leverage Bintray's hijacked publish task
      else Keys.publish
    } dependsOn (Bintray.bintrayEnsureLicenses)

    private def sonatypeRelease(state: sbt.State): Def.Initialize[Task[Unit]] = {
      Def.task {
        // Trick to make sure that 'sonatypeRelease' does not change the name
        import Sonatype.{sonatypeRelease => _}
        runCommandAndRemaining("sonatypeRelease")(state)
        ()
      }
    }

    val releaseEarlyClose: Def.Initialize[Task[Unit]] = Def.taskDyn {
      val state = Keys.state.value
      val underlyingPublisher = ThisPluginKeys.releaseEarlyWith.value
      if (PrivateKeys.releaseEarlyIsSonatype.value) sonatypeRelease(state)
      else Bintray.bintrayRelease
    }

    val releaseEarlyProcess: Def.Initialize[Seq[sbt.TaskKey[Unit]]] = {
      Def.setting(
        Seq(
          ThisPluginKeys.releaseEarlyCheckRequirements,
          DynVer.dynverAssertVersion,
          ThisPluginKeys.releaseEarlyValidatePom,
          ThisPluginKeys.releaseEarlyCheckSnapshotDependencies,
          ThisPluginKeys.releaseEarlyPublish,
          ThisPluginKeys.releaseEarlyClose,
          ThisPluginKeys.releaseEarlySyncToMaven
        )
      )
    }

    val releaseEarly: Def.Initialize[Task[Unit]] = Def.taskDyn {
      val logger = Keys.streams.value.log
      if (!ThisPluginKeys.releaseEarlyInsideCI.value &&
          !ThisPluginKeys.releaseEarlyEnableLocalReleases.value) {
        Def.task(sys.error(Feedback.OnlyCI))
      } else if (noArtifactToPublish.value) {
        val msg = Feedback.skipRelease(Keys.name.value)
        Def.task(logger.info(msg))
      } else {
        val steps = ThisPluginKeys.releaseEarlyProcess.value
        // Return task with unit value at the end
        val initializedSteps = steps.map(_.toTask)
        Def.taskDyn {
          logger.info(Feedback.logReleaseEarly(Keys.name.value))
          StableDef.sequential(initializedSteps, Def.task(()))
        }
      }
    }

    /** Validate POM files for synchronization with Maven Central.
      *
      * Items required:
      *   - Coordinates: groupId, artifactId, version.
      *   - Project: name, description, url.
      *   - License: name, url.
      *   - Developer information: name, email, organization, organizationUrl.
      *   - SCM: connection, developerConnection, url.
      *
      * From: https://blog.idrsolutions.com/2015/06/how-to-upload-your-java-artifact-to-maven-central/.
      */
    val releaseEarlyValidatePom: Def.Initialize[Task[Unit]] = {
      Def.taskDyn {
        // Don't run task on subprojects that don't publish
        if (Keys.publishArtifact.value) validatePomTask
        else Def.task(())
      }
    }

    val releaseEarlyCheckRequirements: Def.Initialize[Task[Unit]] = {
      Def.taskDyn {
        // Don't run task on subprojects that don't publish
        if (Keys.publishArtifact.value) checkRequirementsTask
        else Def.task(())
      }
    }

    val releaseEarlyEnableSyncToMaven: Def.Initialize[Boolean] =
      Def.setting(true)

    val releaseEarlySyncToMaven: Def.Initialize[Task[Unit]] = {
      Def.taskDyn {
        val logger = Keys.streams.value.log
        val projectName = Keys.name.value
        val mustSyncToMaven: Boolean = (
          !PrivateKeys.releaseEarlyIsSonatype.value &&
            ThisPluginKeys.releaseEarlyInsideCI.value &&
            ThisPluginKeys.releaseEarlyEnableSyncToMaven.value &&
            !Keys.isSnapshot.value
        )
        if (mustSyncToMaven) {
          Def.task {
            logger.info(Feedback.logSyncToMaven(projectName))
            bintray.BintrayKeys.bintraySyncMavenCentral.value
          }
        } else Def.task(logger.info(Feedback.skipSyncToMaven(projectName)))
      }
    }

    val saneDefaults: Seq[Setting[_]] = Seq(
      Bintray.bintrayOmitLicense := false,
      Bintray.bintrayReleaseOnPublish := false,
      Bintray.bintrayVcsUrl := {
        // This is necessary to create repos in bintray if they don't exist
        Bintray.bintrayVcsUrl.value
          .orElse(Keys.scmInfo.value.map(_.browseUrl.toString))
          .orElse {
            val url = Keys.pomExtra.value.\\("scm").\\("url").text
            if (url.nonEmpty) Some(url) else sys.error(Feedback.missingVcsUrl)
          }
      }
    )
  }
}

trait Helper {
  import sbt.Keys
  import sbt.State
  import ReleaseEarly.PrivateKeys

  def noArtifactToPublish: Def.Initialize[Task[Boolean]] = Def.task {
    import Keys.publishArtifact
    !(
      publishArtifact.value ||
        publishArtifact.in(sbt.Compile).value ||
        publishArtifact.in(sbt.Test).value
    )
  }

  def checkRequirementsTask: Def.Initialize[Task[Unit]] = Def.task {
    import scala.util.control.Exception.catching
    val logger = Keys.streams.value.log
    val projectName = Keys.name.value
    val useSonatype = PrivateKeys.releaseEarlyIsSonatype.value

    logger.info(Feedback.logCheckRequirements(projectName))

    val bintrayCredentials = {
      if (useSonatype) {
        logger.debug(Feedback.skipBintrayCredentialsCheck(projectName))
        None
      } else {
        catching(classOf[NoSuchElementException])
          .opt(bintray.BintrayKeys.bintrayEnsureCredentials.value)
      }
    }

    val sonatypeCredentials = {
      if (useSonatype) {
        getSonatypeCredentials.orElse {
          // Get extra credentials from optional environment variables
          val extraCredentials = getExtraSonatypeCredentials
          extraCredentials.foreach(persistExtraSonatypeCredentials)
          extraCredentials
        }
      } else {
        logger.debug(Feedback.skipBintrayCredentialsCheck(projectName))
        None
      }
    }

    // If not interactive, it means input has to come from environment
    val missingBintrayCredentials = !useSonatype && bintrayCredentials.isEmpty
    val missingSonatypeCredentials = (
      // True if sonatype is the underlying publisher
      useSonatype && (
        // Or if Bintray publishes a stable version under interactive mode
        !Keys.isSnapshot.value &&
          sonatypeCredentials.isEmpty &&
          !Keys.state.value.interactive
      )
    )

    val Checks = List(
      (missingBintrayCredentials, Feedback.missingBintrayCredentials),
      (missingSonatypeCredentials, Feedback.missingSonatypeCredentials)
    )

    val hasErrors = Checks.foldLeft(false) {
      case (hasError, (predicate, feedback)) =>
        if (predicate) { logger.error(feedback); true } else hasError
    }

    if (hasErrors) sys.error(Feedback.fixRequirementErrors)
  }

  def validatePomTask: Def.Initialize[Task[Unit]] = Def.task {
    val logger = Keys.streams.value.log
    logger.info(Feedback.logValidatePom(Keys.name.value))

    val Checks = List(
      (Keys.scmInfo.value.toList, "scm", Feedback.missingVcsUrl),
      (Keys.developers.value, "developers", Feedback.missingDevelopers),
      (Keys.licenses.value, "licenses", Feedback.forceValidLicense)
    )

    val pom = Keys.pomExtra.value
    val hasErrors = Checks.foldLeft(false) {
      case (hasError, (value, label, feedback)) =>
        if (value.isEmpty && missingNode(pom, label)) {
          logger.error(feedback)
          true
        } else hasError
    }

    // Ensure licenses before releasing
    val useBintray = !PrivateKeys.releaseEarlyIsSonatype.value
    if (useBintray) bintray.BintrayKeys.bintrayEnsureLicenses.value
    if (hasErrors) sys.error(Feedback.fixRequirementErrors)
  }

  def runCommandAndRemaining(command: String): State => State = { st: State =>
    import sbt.complete.Parser
    @annotation.tailrec
    def runCommand(command: String, state: State): State = {
      val nextState = Parser.parse(command, state.combinedParser) match {
        case Right(cmd) => cmd()
        case Left(msg)  => throw sys.error(s"Invalid programmatic input:\n$msg")
      }
      nextState.remainingCommands.toList match {
        case Nil => nextState
        case head :: tail =>
          runCommand(head, nextState.copy(remainingCommands = tail))
      }
    }
    runCommand(command, st.copy(remainingCommands = Nil))
      .copy(remainingCommands = st.remainingCommands)
  }

  import sbtdynver.GitDescribeOutput
  def isDynVerSnapshot(gitInfo: Option[GitDescribeOutput],
                       defaultValue: Boolean): Boolean = {
    val isStable = gitInfo.map { info =>
      info.ref.value.startsWith("v") &&
      (info.commitSuffix.distance <= 0 || info.commitSuffix.sha.isEmpty)
    }
    val isNewSnapshot =
      isStable.map(stable => !stable || defaultValue)
    // Return previous snapshot definition in case users has overridden version
    isNewSnapshot.getOrElse(defaultValue)
  }

  import scala.xml.NodeSeq
  protected def missingNode(pom: NodeSeq, label: String): Boolean =
    pom.\\(label).isEmpty

  /** Get extra sonatype credentials for those that dislike `SONA_*` and
    * system properties. The extra keys start with `SONATYPE` instead of `SONA`.
    */
  protected def getExtraSonatypeCredentials: Option[(String, String)] = {
    for {
      name <- sys.env.get("SONATYPE_USER")
      password <- sys.env.get("SONATYPE_PASSWORD")
    } yield (name, password)
  }

  private val PropertyKeys = ("sona.user", "sona.pass")

  /** Persist extra sonatype credentials reusing the existing system properties.
    *
    * As we cannot access the sbt-bintray cache, we need to use the existing
    * infrastructure to support these extra environment variables.
    */
  protected def persistExtraSonatypeCredentials(
      credentials: (String, String)): Unit = {
    sys.props += PropertyKeys._1 -> credentials._1
    sys.props += PropertyKeys._2 -> credentials._2
  }

  /** Get Sonatype credentials from environment in the same way as sbt-bintray:
    *
    *   1. System properties.
    *   2. Environment variables.
    *
    * This code is copy-pasted from sbt-bintray and is slightly modified.
    */
  protected def getSonatypeCredentials: Option[(String, String)] = {
    val propsCredentials: Option[(String, String)] = {
      for {
        name <- sys.props.get(PropertyKeys._1)
        pass <- sys.props.get(PropertyKeys._2)
      } yield (name, pass)
    }
    propsCredentials.orElse {
      for {
        name <- sys.env.get("SONA_USER")
        pass <- sys.env.get("SONA_PASS")
      } yield (name, pass)
    }
  }
}
