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
    val releaseEarly: TaskKey[Unit] = taskKey("Release early, release often.")
    val releaseEarlyValidatePom: TaskKey[Unit] =
      taskKey("Validate the data to generate a POM file.")
    val releaseEarlySyncToMaven: TaskKey[Unit] =
      taskKey("Synchronize to Maven Central.")
  }
}

object ReleaseEarly {
  import sbt.{Keys, AttributeKey}

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
    releaseEarlySyncToMaven := Defaults.releaseEarlySyncToMaven.value
  ) ++ Defaults.saneDefaults

  object Defaults extends Helper {
    import ReleaseEarlyPlugin.{autoImport => ShadedReleaseEarly}

    // See https://github.com/dwijnand/sbt-dynver/issues/23.
    val isSnapshot: Def.Initialize[Boolean] = Def.setting {
      val customIsSnapshot = DynVer.dynverGitDescribeOutput.value.map { info =>
        info.ref.value.startsWith("v") &&
        (info.commitSuffix.distance <= 0 || info.commitSuffix.sha.isEmpty)
      }
      customIsSnapshot.getOrElse(Keys.isSnapshot.value)
    }

    val releaseEarlyEnableLocalReleases: Def.Initialize[Boolean] =
      Def.setting(false)

    val releaseEarlyInsideCI: Def.Initialize[Boolean] =
      Def.setting(sys.env.get("CI").isDefined)

    val releaseEarly: Def.Initialize[Task[Unit]] = Def.taskDyn {
      if (!releaseEarlyInsideCI.value &&
          !releaseEarlyEnableLocalReleases.value)
        Def.task(sys.error(Feedback.OnlyCI))
      else Def.task(runCommand("release")(Keys.state.value))
    }

    val releaseEarlyValidatePom: Def.Initialize[Task[Unit]] = {
      import Keys.{publishArtifact, scmInfo, licenses}
      import bintray.BintrayKeys.bintrayEnsureLicenses
      Def.taskDyn {
        // Avoid failing on root
        if (publishArtifact.value) {
          Def.task {
            if (scmInfo.value.isEmpty)
              sys.error(Feedback.forceDefinitionOfScmInfo)
            if (licenses.value.isEmpty)
              sys.error(Feedback.forceValidLicense)
            bintrayEnsureLicenses.value
          }
        } else Def.task(())
      }
    }

    val releaseEarlySyncToMaven: Def.Initialize[Task[Unit]] = {
      Def.taskDyn {
        if (releaseEarlyInsideCI.value)
          bintray.BintrayKeys.bintraySyncMavenCentral
        else Def.task(())
      }
    }

    /** Define keys for every release step so that users of the plugin can
      * programmatically manipulate the release pipeline without making their
      * own from scratch. This is accessible from `releaseEarlyKeyedProcess`.
      */
    val releaseProcessKeys: Seq[AttributeKey[_]] = {
      List(
        AttributeKey("inquireVersions", "Release step to inquire versions."),
        DynVer.dynverAssertVersion.key,
        ShadedReleaseEarly.releaseEarlyValidatePom.key,
        AttributeKey("checkSnapshotDependencies", "Check snapshots."),
        AttributeKey("publishArtifacts", "Publish all artifacts."),
        Bintray.bintrayRelease.key,
        ShadedReleaseEarly.releaseEarlySyncToMaven.key
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
        inquireVersions,
        releaseStepTask(DynVer.dynverAssertVersion),
        releaseStepTask(ShadedReleaseEarly.releaseEarlyValidatePom),
        checkSnapshotDependencies,
        publishArtifacts,
        releaseStepTask(Bintray.bintrayRelease),
        releaseStepTask(ShadedReleaseEarly.releaseEarlySyncToMaven)
      )
    }

    val releaseEarlyKeyedProcess: Def.Initialize[Seq[KeyedReleaseStep]] = {
      assert(releaseProcessKeys.size == releaseProcess.size)
      Def.setting(releaseProcessKeys.zip(releaseProcess).map {
        case (key, step) => KeyedReleaseStep(key, step)
      })
    }

    val saneDefaults: Seq[Setting[_]] = Seq(
      // We want to cross-build at the CI level, using parallelism
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
}
