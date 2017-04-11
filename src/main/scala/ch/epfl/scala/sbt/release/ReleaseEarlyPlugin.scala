package ch.epfl.scala.sbt.release

import sbt.Plugin

object ReleaseEarlyPlugin extends Plugin {
  object autoImport extends ReleaseEarlyKeys
}

trait ReleaseEarlyKeys {
  import sbt.taskKey
  val releaseEarly = taskKey[Unit]("Release early, release often.")
}
