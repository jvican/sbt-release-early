package ch.epfl.scala.sbt.release

import sbt.librarymanagement.ivy.{InlineIvyConfiguration, IvyDependencyResolution}
import sbt.{AutoPlugin, Def, PluginTrigger, Plugins, Setting, Task}

object ReleaseEarlyPlugin extends AutoPlugin {
  val autoImport = AutoImported

  import com.jsuereth.sbtpgp.SbtPgp
  override def trigger: PluginTrigger = allRequirements
  override def requires: Plugins =
    sbtdynver.DynVerPlugin && bintray.BintrayPlugin && xerial.sbt.Sonatype && SbtPgp

  override def globalSettings: Seq[Def.Setting[_]] =
    ReleaseEarly.globalSettings
  override def projectSettings: Seq[Def.Setting[_]] =
    ReleaseEarly.projectSettings
  override def buildSettings: Seq[Def.Setting[_]] =
    ReleaseEarly.buildSettings
}

object AutoImported
    extends ReleaseEarlyKeys.ReleaseEarlySettings
    with ReleaseEarlyKeys.ReleaseEarlyTasks

object ReleaseEarlyKeys {
  import sbt.{taskKey, settingKey, TaskKey, SettingKey, Global}
  trait UnderlyingPublisher {
    // Set if this publisher requires an extra task for synchronization with maven central
    val explicitMavenCentralSynchronizationTask: Option[TaskKey[Unit]] = None
    // Set if this publisher requires an explicit step to validate licenses
    val explicitLicenseCheck: Option[TaskKey[Unit]] = None
  }
  case object BintrayPublisher extends UnderlyingPublisher {
    override val explicitMavenCentralSynchronizationTask =
      Some(bintray.BintrayKeys.bintraySyncMavenCentral)
    override val explicitLicenseCheck =
      Some(bintray.BintrayKeys.bintrayEnsureLicenses)
  }
  case object SonatypePublisher extends UnderlyingPublisher 

  trait ReleaseEarlySettings {
    val SonatypePublisher: ReleaseEarlyKeys.SonatypePublisher.type = ReleaseEarlyKeys.SonatypePublisher
    val BintrayPublisher: ReleaseEarlyKeys.BintrayPublisher.type = ReleaseEarlyKeys.BintrayPublisher

    // Note that we enforce that these settings will always be scoped globally
    private val localReleaseEarlyEnableLocalReleases: SettingKey[Boolean] =
      SettingKey("releaseEarlyEnableLocalReleases", "Enable local releases.")
    val releaseEarlyEnableLocalReleases: SettingKey[Boolean] =
      localReleaseEarlyEnableLocalReleases in Global
    private val localReleaseEarlyInsideCI: SettingKey[Boolean] =
      settingKey("Detect whether sbt is running inside the CI.")
    val releaseEarlyInsideCI: SettingKey[Boolean] = localReleaseEarlyInsideCI in Global
    private val localReleaseEarlyEnableInstantReleases: SettingKey[Boolean] =
      SettingKey("releaseEarlyEnableInstantReleases", "Enable instant releases. By default, true.")
    val releaseEarlyEnableInstantReleases: SettingKey[Boolean] =
      localReleaseEarlyEnableInstantReleases in Global

    val releaseEarlyBypassSnapshotCheck: SettingKey[Boolean] =
      settingKey("Bypass snapshots check, not failing if snapshots are found.")
    val releaseEarlyProcess: SettingKey[Seq[TaskKey[Unit]]] =
      settingKey("Release process executed by `releaseEarly`.")
    val releaseEarlyWith: SettingKey[UnderlyingPublisher] =
      settingKey("Specify the publisher to publish your artifacts.")
    val releaseEarlyNoGpg: SettingKey[Boolean] =
      settingKey("Don't use `publishSigned` to `publish` stable releases.")
    val releaseEarlyEnableSyncToMaven: SettingKey[Boolean] =
      settingKey("Enable synchronization to Maven Central for git tags.")
    val releaseEarlyIgnoreLocalRepository: SettingKey[Boolean] =
      settingKey("Ignore ivy local repository when sbt-release-early looks for released artifacts.")
  }

  trait ReleaseEarlyTasks {
    val releaseEarly: TaskKey[Unit] =
      taskKey("Release early, release often.")
    val releaseEarlyValidatePom: TaskKey[Unit] =
      taskKey("Validate the data to generate a POM file.")
    val releaseEarlySyncToMaven: TaskKey[Unit] =
      taskKey("Synchronize to Maven Central.")
    val releaseEarlyCheckRequirements: TaskKey[Unit] =
      taskKey("Check the requirements of the environment.")
    val releaseEarlyCheckSnapshotDependencies: TaskKey[Unit] =
      taskKey("Check snapshot dependencies before the release.")
    val releaseEarlyPublish: TaskKey[Unit] =
      taskKey(s"Publish artifact. Defaults to ${sbt.Keys.publish.key.label}.")
    val releaseEarlyClose: TaskKey[Unit] =
      taskKey("Materialize the release by closing staging repositories.")
    val releaseEarlySonatypeCredentials: TaskKey[Seq[sbt.Credentials]] =
      taskKey("Fetch sonatype credentials from env and persists them.")
  }
}

object ReleaseEarly {
  import sbt.{Keys, Tags}

  import ReleaseEarlyPlugin.autoImport._
  import xerial.sbt.Sonatype.{SonatypeCommand => SonatypeCommands}
  import xerial.sbt.Sonatype.{autoImport => Sonatype}
  import bintray.BintrayPlugin.{autoImport => Bintray}
  import sbtdynver.DynVerPlugin.{autoImport => DynVer}
  import com.jsuereth.sbtpgp.SbtPgp.{autoImport => Pgp}

  final val SingleThreadedRelease = Tags.Tag("single-threaded-release")
  final val ExclusiveReleaseTag = Tags.exclusive(SingleThreadedRelease)

  val globalSettings: Seq[Setting[_]] = Seq(
    releaseEarlyInsideCI := Defaults.releaseEarlyInsideCI.value,
    releaseEarlyEnableLocalReleases := Defaults.releaseEarlyEnableLocalReleases.value,
    releaseEarlyEnableInstantReleases := Defaults.releaseEarlyEnableInstantReleases.value,
    Keys.credentials := Defaults.releaseEarlySonatypeCredentials.value,
    // This is not working for now, see https://github.com/sbt/sbt-pgp/issues/111
    // When it's fixed, remove the scoped key in `buildSettings` and this will work
    Pgp.pgpPassphrase := Defaults.pgpPassphrase.value,
    Keys.concurrentRestrictions += ExclusiveReleaseTag,
    releaseEarlyWith := Defaults.releaseEarlyWith.value,
    releaseEarlyBypassSnapshotCheck := Defaults.releaseEarlyBypassSnapshotChecks.value,
    releaseEarlyNoGpg := Defaults.releaseEarlyNoGpg.value,
    releaseEarlyEnableSyncToMaven := Defaults.releaseEarlyEnableSyncToMaven.value,
    releaseEarlyIgnoreLocalRepository := Defaults.releaseEarlyIgnoreLocalRepository.value,
  )

  val buildSettings: Seq[Setting[_]] = Seq(
    Keys.isSnapshot := Defaults.isSnapshot.value,
    Pgp.pgpPassphrase := Defaults.pgpPassphrase.value
  )

  // TODO(jvican): Rethink the proper scopes of all these keys.
  val projectSettings: Seq[Setting[_]] = Seq(
    Keys.isSnapshot := Defaults.isSnapshot.value,
    Keys.publishTo := Defaults.releaseEarlyPublishTo.value,
    releaseEarly := Defaults.releaseEarly.value,
    releaseEarlySyncToMaven := Defaults.releaseEarlySyncToMaven.value,
    releaseEarlyValidatePom := Defaults.releaseEarlyValidatePom.value,
    releaseEarlyCheckRequirements := Defaults.releaseEarlyCheckRequirements.value,
    releaseEarlyCheckSnapshotDependencies := Defaults.releaseEarlyCheckSnapshotDependencies.value,
    releaseEarlyPublish := Defaults.releaseEarlyPublish.value,
    releaseEarlyClose := Defaults.releaseEarlyClose.value,
    releaseEarlyProcess := Defaults.releaseEarlyProcess.value
  ) ++ Defaults.saneDefaults

  object Defaults extends Helper {
    import ReleaseEarlyPlugin.{autoImport => ThisPluginKeys}
    import ReleaseEarlyKeys.{UnderlyingPublisher, SonatypePublisher, BintrayPublisher}

    /* Sbt bug: `Def.sequential` here produces 'Illegal dynamic reference' when
     * used inside `Def.taskDyn`. This is reported upstream, unclear if it can be fixed. */
    private val StableDef = new sbt.internal.TaskSequential {}

    // See https://github.com/dwijnand/sbt-dynver/issues/23.
    val isSnapshot: Def.Initialize[Boolean] = Def.setting {
      isDynVerSnapshot(DynVer.dynverGitDescribeOutput.value, Keys.isSnapshot.value)
    }

    val pgpPassphrase: Def.Initialize[Option[Array[Char]]] = Def.setting {
      val currentPassword = Pgp.pgpPassphrase.value
      val logger = Keys.sLog.value
      if (currentPassword.isEmpty) {
        logger.debug(Feedback.LogFetchPgpCredentials)
        getPgpPassphraseFromEnvironment
      } else currentPassword
    }
    
    val releaseEarlyWith: Def.Initialize[UnderlyingPublisher] =
      Def.setting(ReleaseEarlyKeys.SonatypePublisher)


    private val sonatypeStagingId = sbt.librarymanagement
      .MavenRepository("sonatype-staging-id", "https://oss.sonatype.org/service/local/staging")

    val releaseEarlyPublishTo: Def.Initialize[Task[Option[sbt.Resolver]]] = {
      import sbt.librarymanagement.syntax.toRepositoryName
      Def.taskDyn {
        // It is not necessary to use a dynamic setting here.
        val logger = Keys.sLog.value
        releaseEarlyWith.value match {
          case SonatypePublisher => Def.task {
            // Sonatype requires instrumentation of publishTo to work.
            // Reference: https://github.com/xerial/sbt-sonatype#buildsbt
            val projectVersion = Keys.version.value
            if (isOldSnapshot(projectVersion))
              logger.error(Feedback.unsupportedSnapshot(projectVersion))

            Sonatype.sonatypeStagingRepositoryProfile.?.value match {
              case Some(profile) =>
                val root = sonatypeStagingId + s"/deployByRepositoryId/${profile.repositoryId}"
                Some(sonatypeStagingId.name at root)
              case None => Some(sbt.Opts.resolver.sonatypeStaging)
            }
          }
          case BintrayPublisher => (Keys.publishTo in Bintray.bintray)
        }
      }
    }

    private final val SonatypeRealm = "Sonatype Nexus Repository Manager"
    private final val SonatypeHost = "oss.sonatype.org"
    val releaseEarlySonatypeCredentials: Def.Initialize[Task[Seq[sbt.Credentials]]] = {
      import sbt.{Credentials, DirectCredentials, FileCredentials}
      Def.task {
        val logger = Keys.streams.value.log
        val currentCredentials = Keys.credentials.value
        val existingSonatypeCredential = currentCredentials.find {
          case credentials: DirectCredentials =>
            credentials.realm == SonatypeRealm && credentials.host == SonatypeHost
          // The code in sbt-sonatype does not use file credentials, this is safe.
          case fileCredentials: FileCredentials => false
        }

        if (existingSonatypeCredential.isEmpty) {
          getSonatypeCredentials.orElse(getExtraSonatypeCredentials) match {
            case Some((user, passwd)) =>
              logger.debug(Feedback.LogAddSonatypeCredentials)
              val newCredentials =
                Credentials(SonatypeRealm, SonatypeHost, user, passwd)
              currentCredentials :+ newCredentials
            case _ => currentCredentials
          }
        } else currentCredentials
      }
    }

    val releaseEarlyEnableLocalReleases: Def.Initialize[Boolean] =
      Def.setting(false)

    val releaseEarlyEnableInstantReleases: Def.Initialize[Boolean] = Def.setting(true)

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
          val snapshots =
            moduleIds.filter(m => m.isChanging || isOldSnapshot(m.revision))
          if (snapshots.nonEmpty)
            sys.error(Feedback.detectedSnapshotsDependencies(snapshots))
          else Def.task(())
        } else Def.task(())
      }
    }

    val releaseEarlyPublish: Def.Initialize[Task[Unit]] = Def.taskDyn {
      releaseEarlyWith.value match {
        case SonatypePublisher =>
          // If sonatype, always use `publishSigned` and ignore `publish`
          sonatypeRelease(Keys.state.value).tag(SingleThreadedRelease)
        case BintrayPublisher =>
          // Use signed for stable releases
          if (!Keys.isSnapshot.value && !ThisPluginKeys.releaseEarlyNoGpg.value) Pgp.PgpKeys.publishSigned
          // Else, use the normal hijacked bintray publish task
          else Keys.publish
      }
    }

    private def sonatypeRelease(state: sbt.State): Def.Initialize[Task[Unit]] = {
      // sbt-sonatype needs these task to run sequentially :(
      Def.task {
        val logger = Keys.streams.value.log
        val projectId = sbt.Reference.display(Keys.thisProjectRef.value)
        logger.info(Feedback.logReleaseSonatype(projectId))
        // Trick to make sure that 'sonatypeRelease' does not change the name
        import SonatypeCommands.{sonatypeRelease => _, sonatypeOpen => _}
        // We don't use `sonatypeOpen` because `publishSigned` deduplicates the repository
        val toRun = s";$projectId/publishSigned;sonatypeRelease"
        runCommandAndRemaining(toRun)(state)
        ()
      }
    }

    /* For now, this task only execute `bintrayRelease`, `sonatypeRelease`
     * is tightly tied to `publishSigned` and cannot be easily decoupled.
     * Both have to be executed one after the other on and exclusively,
     * meaning that concurrency is not accepted. */
    val releaseEarlyClose: Def.Initialize[Task[Unit]] = Def.taskDyn {
      val logger = Keys.streams.value.log
      val projectName = Keys.name.value
      releaseEarlyWith.value match {
        case SonatypePublisher => Def.task(())
        case BintrayPublisher =>
          logger.info(Feedback.logReleaseBintray(projectName))
          Bintray.bintrayRelease
      }
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

    import sbt.librarymanagement.Resolver
    private final val LocalResolvers =
      List(Resolver.defaultLocal, Resolver.mavenLocal, Resolver.publishMavenLocal)

    val releaseEarly: Def.Initialize[Task[Unit]] = Def.taskDyn {
      val logger = Keys.streams.value.log
      val projectName = Keys.name.value
      if (!ThisPluginKeys.releaseEarlyInsideCI.value &&
          !ThisPluginKeys.releaseEarlyEnableLocalReleases.value) {
        Def.task(sys.error(Feedback.OnlyCI))
      } else if (noArtifactToPublish.value) {
        Def.task(logger.info(Feedback.skipRelease(projectName)))
      } else if (!ThisPluginKeys.releaseEarlyEnableInstantReleases.value && Keys.isSnapshot.value) {
        Def.task(logger.info(Feedback.skipInstantRelease(projectName, Keys.version.value)))
      } else {
        Def.taskDyn {
          import sbt.util.Logger.{Null => NoLogger}
          val logger = Keys.streams.value.log

          // Important to make it transitive, we just want to check if a jar exists
          val name = Keys.name.value
          val moduleID = Keys.projectID.value.intransitive()
          val scalaModule = Keys.scalaModuleInfo.value

          // If it's another thing, just fail! We must have an inline ivy config here.
          val ivyConfig0 = Keys.ivyConfiguration.value.asInstanceOf[InlineIvyConfiguration]

          // We can do this because we resolve intransitively and nobody but this task publishes
          val ivyConfig1 = ivyConfig0.withChecksums(Vector()).withLock(None)
          val ignoreLocalRepo = ThisPluginKeys.releaseEarlyIgnoreLocalRepository.value
          val ivyConfig = if (ignoreLocalRepo) {
            val remoteResolvers = ivyConfig0.resolvers.filterNot(r => LocalResolvers.contains(r))
            ivyConfig1.withResolvers(remoteResolvers)
          } else ivyConfig1

          val resolution = IvyDependencyResolution(ivyConfig)
          sbt.IO.withTemporaryDirectory { tmpRetrieveDir =>
            logger.info(Feedback.logResolvingModule(moduleID.toString))
            val result = resolution.retrieve(moduleID, scalaModule, tmpRetrieveDir, NoLogger)
            result match {
              case Left(_) => // Trigger release for unexisting module, yay!
                logger.info(Feedback.logReleaseEarly(name))
                val steps = ThisPluginKeys.releaseEarlyProcess.value
                StableDef.sequential(steps.map(_.toTask), Def.task(()))
              case Right(resolved) => // Skip release, module has already been published!
                Def.task(logger.warn(Feedback.logAlreadyPublishedModule(name, moduleID.toString)))
            }
          }
        }
      }
    }

    val releaseEarlyIgnoreLocalRepository: Def.Initialize[Boolean] = Def.setting(true)

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
      Def.setting(releaseEarlyWith.value.explicitMavenCentralSynchronizationTask.isDefined)

    val releaseEarlyNoGpg: Def.Initialize[Boolean] = Def.setting(false)

    val releaseEarlySyncToMaven: Def.Initialize[Task[Unit]] = {
      Def.taskDyn {
        val logger = Keys.streams.value.log
        val projectName = Keys.name.value
        val syncTask = releaseEarlyWith.value.explicitMavenCentralSynchronizationTask
        val mustSyncToMaven: Boolean = (
          syncTask.isDefined &&
            ThisPluginKeys.releaseEarlyInsideCI.value &&
            ThisPluginKeys.releaseEarlyEnableSyncToMaven.value &&
            !Keys.isSnapshot.value
        )
        if (mustSyncToMaven) {
          Def.task {
            logger.info(Feedback.logSyncToMaven(projectName))
            syncTask.get.value
          }
        } else if (syncTask.isDefined) {
          Def.task(logger.info(Feedback.skipSyncToMaven(projectName)))
        } else Def.task(())
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
  import sbt.util.Logger
  import ReleaseEarlyKeys.{SonatypePublisher, BintrayPublisher}

  def isOldSnapshot(version: String): Boolean =
    version.endsWith("-SNAPSHOT")

  def noArtifactToPublish: Def.Initialize[Task[Boolean]] = Def.task {
    import Keys.publishArtifact
    !(
      publishArtifact.value ||
        publishArtifact.in(sbt.Compile).value ||
        publishArtifact.in(sbt.Test).value
    )
  }

  private def checkSonatypeRequirements(
    projectName: String,
    logger: Logger, 
    collectAndReportFeedback: ((Boolean, String)*) => Unit
  ): Def.Initialize[Task[Unit]] = Def.task {
    import ReleaseEarlyPlugin.{autoImport => ThisPluginKeys}

    logger.debug(Feedback.skipBintrayCredentialsCheck(projectName))
    val sonatypeCredentials = getSonatypeCredentials.orElse(getExtraSonatypeCredentials)
    sonatypeCredentials.foreach(persistExtraSonatypeCredentials)

    val missingSonatypeCredentials = {
      sonatypeCredentials.isEmpty &&
      !Keys.isSnapshot.value &&
      !Keys.state.value.interactive
    }

    val sonatypeInconsistentState = ThisPluginKeys.releaseEarlyNoGpg.value
    collectAndReportFeedback(
      (missingSonatypeCredentials, Feedback.missingSonatypeCredentials),
      (sonatypeInconsistentState, Feedback.SonatypeInconsistentGpgState)
    )

  }
  
  private def checkBintrayRequirements(
    logger: Logger, 
    collectAndReportFeedback: ((Boolean, String)*) => Unit
  ): Def.Initialize[Task[Unit]] = Def.task {
    import ReleaseEarlyPlugin.{autoImport => ThisPluginKeys}
    import scala.util.control.Exception.catching
    
    val missingBintrayCredentials = {
        catching(classOf[NoSuchElementException])
          .opt(bintray.BintrayKeys.bintrayEnsureCredentials.value)
          .isEmpty
      }

    val bintrayInconsistentState =
      ThisPluginKeys.releaseEarlyEnableSyncToMaven.value &&
        ThisPluginKeys.releaseEarlyNoGpg.value

    collectAndReportFeedback(
      (missingBintrayCredentials, Feedback.missingBintrayCredentials),
      (bintrayInconsistentState, Feedback.BintrayInconsistentGpgState)
    )
  }


  def checkRequirementsTask: Def.Initialize[Task[Unit]] = Def.taskDyn {
    import ReleaseEarlyPlugin.{autoImport => ThisPluginKeys}
    val logger = Keys.streams.value.log
    val projectName = Keys.name.value
    logger.info(Feedback.logCheckRequirements(projectName))

    def check(checks: (Boolean, String)*): Unit = {
      val errors = checks.collect { case (true, feedback) => logger.error(feedback) }
      if (errors.nonEmpty) sys.error(Feedback.fixRequirementErrors)
    }

    ThisPluginKeys.releaseEarlyWith.value match {
      case SonatypePublisher => checkSonatypeRequirements(projectName, logger, check)
      case BintrayPublisher => checkBintrayRequirements(logger, check)
    }
  }

  def validatePomTask: Def.Initialize[Task[Unit]] = Def.taskDyn {
    import ReleaseEarlyPlugin.{autoImport => ThisPluginKeys}
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

    if (hasErrors) sys.error(Feedback.fixRequirementErrors)

    ThisPluginKeys.releaseEarlyWith.value.explicitLicenseCheck.getOrElse(Def.task(()))
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
          runCommand(head.commandLine, nextState.copy(remainingCommands = tail))
      }
    }
    runCommand(command, st.copy(remainingCommands = Nil))
      .copy(remainingCommands = st.remainingCommands)
  }

  import sbtdynver.GitDescribeOutput
  def isDynVerSnapshot(gitInfo: Option[GitDescribeOutput], defaultValue: Boolean): Boolean = {
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

  private val PgpPasswordId = "PGP_PASSWORD"
  private val PgpPassId = "PGP_PASS"
  protected def getPgpPassphraseFromEnvironment: Option[Array[Char]] =
    sys.env.get(PgpPasswordId).orElse(sys.env.get(PgpPassId)).map(_.toArray)

  private val PropertyKeys = ("sona.user", "sona.pass")

  /** Persist extra sonatype credentials reusing the existing system properties.
    *
    * As we cannot access the sbt-bintray cache, we need to use the existing
    * infrastructure to support these extra environment variables.
    */
  protected def persistExtraSonatypeCredentials(credentials: (String, String)): Unit = {
    if (!sys.props.contains(PropertyKeys._1) || !sys.props.contains(PropertyKeys._2)) {
      sys.props += PropertyKeys._1 -> credentials._1
      sys.props += PropertyKeys._2 -> credentials._2
    }
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
