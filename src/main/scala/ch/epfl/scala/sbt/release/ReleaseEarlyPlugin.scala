package ch.epfl.scala.sbt.release

import sbt.{AutoPlugin, Def, PluginTrigger, Plugins, Setting, Task}

object ReleaseEarlyPlugin extends AutoPlugin {
  object autoImport
      extends ReleaseEarlyKeys.ReleaseEarlySettings
      with ReleaseEarlyKeys.ReleaseEarlyTasks

  override def trigger: PluginTrigger = allRequirements
  override def requires: Plugins =
    sbtrelease.ReleasePlugin && sbtdynver.DynVerPlugin && bintray.BintrayPlugin
  override def projectSettings: Seq[Def.Setting[_]] =
    super.projectSettings ++ ReleaseEarly.projectSettings
}

object ReleaseEarlyKeys {
  import sbt.{taskKey, settingKey, TaskKey, SettingKey}

  trait ReleaseEarlySettings {
    import scala.language.existentials
    import sbtrelease.ReleasePlugin.autoImport.ReleaseStep
    case class KeyedReleaseStep(key: sbt.AttributeKey[_], step: ReleaseStep)

    val releaseEarlyEnableLocalReleases: SettingKey[Boolean] =
      settingKey("Enable local releases.")
    val releaseEarlyInsideCI: SettingKey[Boolean] =
      settingKey("Detect whether sbt is running inside the CI.")
    val releaseEarlyKeyedProcess: SettingKey[Seq[KeyedReleaseStep]] =
      settingKey("Release process with keys for programmatic manipulation.")
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
  }
}

object ReleaseEarly {
  import sbt.{Keys, AttributeKey, State}

  import ReleaseEarlyPlugin.autoImport._
  import sbtrelease.ReleasePlugin.{autoImport => SbtRelease}
  import bintray.BintrayPlugin.{autoImport => Bintray}
  import sbtdynver.DynVerPlugin.{autoImport => DynVer}

  val projectSettings: Seq[Setting[_]] = Seq(
    Keys.isSnapshot := Defaults.isSnapshot.value,
    releaseEarly := Defaults.releaseEarly.value,
    releaseEarlyInsideCI := Defaults.releaseEarlyInsideCI.value,
    releaseEarlyEnableLocalReleases := Defaults.releaseEarlyEnableLocalReleases.value,
    SbtRelease.releaseProcess := Defaults.releaseProcess,
    releaseEarlyKeyedProcess := Defaults.releaseEarlyKeyedProcess.value,
    releaseEarlySyncToMaven := Defaults.releaseEarlySyncToMaven.value,
    releaseEarlyValidatePom := Defaults.releaseEarlyValidatePom.value,
    releaseEarlyCheckRequirements := Defaults.releaseEarlyCheckRequirements.value
  ) ++ Defaults.saneDefaults

  object Defaults extends Helper {
    import ReleaseEarlyPlugin.{autoImport => ThisPluginKeys}

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
      val isStable = DynVer.dynverGitDescribeOutput.value.map { info =>
        info.ref.value.startsWith("v") &&
        (info.commitSuffix.distance <= 0 || info.commitSuffix.sha.isEmpty)
      }
      val isNewSnapshot =
        isStable.map(stable => !stable || Keys.isSnapshot.value)
      // If it's not a regular snapshot and stable, then it's dynver snapshot
      isNewSnapshot.getOrElse(true)
    }

    val releaseEarlyEnableLocalReleases: Def.Initialize[Boolean] =
      Def.setting(false)

    val releaseEarlyInsideCI: Def.Initialize[Boolean] =
      Def.setting(sys.env.get("CI").isDefined)

    val releaseEarly: Def.Initialize[Task[Unit]] = Def.taskDyn {
      if (!ThisPluginKeys.releaseEarlyInsideCI.value &&
          !ThisPluginKeys.releaseEarlyEnableLocalReleases.value)
        Def.task(sys.error(Feedback.OnlyCI))
      else Def.task(runCommand("release")(Keys.state.value))
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
      import bintray.BintrayKeys.bintrayEnsureLicenses
      Def.taskDyn {
        // Don't run task on subprojects that don't publish
        if (Keys.publishArtifact.value) {
          Def.task {
            val logger = Keys.state.value.log
            var errors = 0
            if (Keys.scmInfo.value.isEmpty &&
                missingNode(Keys.pomExtra.value, "scm")) {
              errors += 1
              logger.error(Feedback.forceDefinitionOfScmInfo)
            }
            if (Keys.developers.value.isEmpty &&
                missingNode(Keys.pomExtra.value, "developers")) {
              errors += 1
              logger.error(Feedback.forceDefinitionOfDevelopers)
            }
            if (Keys.licenses.value.isEmpty &&
                missingNode(Keys.pomExtra.value, "licenses")) {
              errors += 1
              logger.error(Feedback.forceValidLicense)
            }

            // Ensure licenses before releasing
            bintrayEnsureLicenses.value

            // There is no way to check if the logger has errors...
            if (errors > 0) sys.error(Feedback.fixRequirementErrors)
          }
        } else Def.task(())
      }
    }

    val releaseEarlyCheckRequirements: Def.Initialize[Task[Unit]] = {
      import scala.util.control.Exception.catching
      Def.taskDyn {
        // Don't run task on subprojects that don't publish
        if (Keys.publishArtifact.value) {
          Def.task {
            var errors = 0
            val logger = Keys.state.value.log
            val bintrayCredentials =
              catching(classOf[NoSuchElementException])
                .opt(Bintray.bintrayEnsureCredentials.value)
            if (bintrayCredentials.isEmpty) {
              errors += 1
              logger.error(Feedback.missingBintrayCredentials)
            }

            // We cannot cache this, bintray cache is private.
            val sonatypeCredentials = getSonatypeCredentials
            if (sonatypeCredentials.isEmpty &&
                !Keys.isSnapshot.value && // We don't sync in snapshots
                !Keys.state.value.interactive) {
              errors += 1
              logger.error(Feedback.missingSonatypeCredentials)
            }

            // There is no way to check if the logger has errors...
            if (errors > 0) sys.error(Feedback.fixRequirementErrors)
          }
        } else Def.task(())
      }
    }

    val releaseEarlySyncToMaven: Def.Initialize[Task[Unit]] = {
      Def.taskDyn {
        if (ThisPluginKeys.releaseEarlyInsideCI.value && !Keys.isSnapshot.value)
          bintray.BintrayKeys.bintraySyncMavenCentral
        else Def.task(())
      }
    }

    val setDynVersion: SbtRelease.ReleaseStep = { (st: State) =>
      val extracted = sbt.Project.extract(st)
      val releaseVersion = extracted.get(Keys.version)
      // Trick sbt release -- next version is not used.
      val nextVersion = releaseVersion
      st.put(SbtRelease.ReleaseKeys.versions, (releaseVersion, nextVersion))
    }

    /** Define keys for every release step so that users of the plugin can
      * programmatically manipulate the release pipeline without making their
      * own from scratch. This is accessible from `releaseEarlyKeyedProcess`. */
    val releaseProcessKeys: Seq[AttributeKey[_]] = {
      List(
        ThisPluginKeys.releaseEarlyCheckRequirements.key,
        DynVer.dynverAssertVersion.key,
        AttributeKey("setDynVersion", "Release step to set dynver versions."),
        ThisPluginKeys.releaseEarlyValidatePom.key,
        AttributeKey("checkSnapshotDependencies", "Check snapshots."),
        AttributeKey("publishArtifacts", "Publish all artifacts."),
        Bintray.bintrayRelease.key,
        ThisPluginKeys.releaseEarlySyncToMaven.key
      )
    }

    import SbtRelease.{ReleaseStep, releaseStepTask}

    /** Define an opinionated release process.
      *
      * Differences with other release processes:
      *
      * 1. It removes any step involving git (version of project is embedded in
      *    git tags, so we don't need to push to master version updates).
      * 2. It does not run tests, since it assumes that tests are already run
      *    by the CI before executing this.
      */
    val releaseProcess: Seq[ReleaseStep] = {
      import sbtrelease.ReleaseStateTransformations._
      List[ReleaseStep](
        releaseStepTask(ThisPluginKeys.releaseEarlyCheckRequirements),
        releaseStepTask(DynVer.dynverAssertVersion),
        setDynVersion,
        releaseStepTask(ThisPluginKeys.releaseEarlyValidatePom),
        checkSnapshotDependencies,
        publishArtifacts,
        releaseStepTask(Bintray.bintrayRelease),
        releaseStepTask(ThisPluginKeys.releaseEarlySyncToMaven)
      )
    }

    val releaseEarlyKeyedProcess: Def.Initialize[Seq[KeyedReleaseStep]] = {
      assert(releaseProcessKeys.size == releaseProcess.size)
      Def.setting(releaseProcessKeys.zip(releaseProcess).map {
        case (key, step) => KeyedReleaseStep(key, step)
      })
    }

    val saneDefaults: Seq[Setting[_]] = Seq(
      // We want to cross-build at the CI level, using different jobs
      SbtRelease.releaseCrossBuild := false,
      SbtRelease.releasePublishArtifactsAction := Keys.publish.value,
      Bintray.bintrayOmitLicense := false
    )
  }
}

trait Helper {
  import sbt.State

  protected def runCommand(command: String): (State) => State = { st: State =>
    import sbt.complete.Parser
    @annotation.tailrec
    def runCommand0(command: String, state: State): State = {
      val nextState = Parser.parse(command, state.combinedParser) match {
        case Right(cmd) => cmd()
        case Left(msg) =>
          throw sys.error(s"Invalid programmatic input:\n$msg")
      }
      nextState.remainingCommands.toList match {
        case Nil => nextState
        case head :: tail =>
          runCommand0(head, nextState.copy(remainingCommands = tail))
      }
    }
    runCommand0(command, st.copy(remainingCommands = Nil))
      .copy(remainingCommands = st.remainingCommands)
  }

  import scala.xml.NodeSeq
  protected def missingNode(pom: NodeSeq, label: String): Boolean =
    pom.\\(label).isEmpty

  /** Get Sonatype credentials from the environment. Order:
    *
    *   1. System properties.
    *   2. Environment variables.
    *
    * This code is copy-pasted from sbt-bintray and is slightly modified.
    */
  protected def getSonatypeCredentials: Option[(String, String)] = {
    val propsCredentials: Option[(String, String)] = {
      for {
        name <- sys.props.get("sona.user")
        pass <- sys.props.get("sona.pass")
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
