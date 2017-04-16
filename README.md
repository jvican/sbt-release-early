# Meet `sbt-release-early` [![Build Status](https://platform-ci.scala-lang.org/api/badges/scalacenter/sbt-release-early/status.svg)](https://platform-ci.scala-lang.org/scalacenter/sbt-release-early)

`sbt-release-early` is an sbt plugin to follow the maxim
`"Release early, release often"`. The major goal is to provide out-of-the-box
support for automatic releases, with some room for customization.

## Use

Not ready yet. Coming soon.

## Dependencies

This plugin relies on the following sbt plugins:

* [`sbt-dynver`](https://github.com/dwijnand/sbt-dynver), version `0.2.0`.
* [`sbt-pgp`](https://github.com/sbt/sbt-pgp), version `1.0.0`.
* [`sbt-bintray`](https://github.com/sbt/sbt-bintray), version `0.3.0`.

If you already depend on them, remove them from your `plugins.sbt` file.

## Requirements

If you want to use `sbt-release-early`, you need to:
  
* Use git and have it in the `PATH`.
* Have a [Bintray](https://github.com/sbt/sbt-bintray) account.
 
To synchronize your artifacts with Maven Central, you need a [Sonatype](https://www.sonatype.com/)
account. This feature is optional, but enabled by default for releases via tag.

## In a nutshell

Every time you *push a commit* to a branch, `sbt-release-early` will release an
artifact with a version derived from the git metadata (version and distance from
last git tag). For example, `0.3.0+10-4f489199`, where `10` is the distance
and the suffix is the commit hash.

Every time you *push a tag* to a branch, `sbt-release-early` will release an
artifact with that tag version. This feature is usually used to cut final releases,
for example, `v0.1.0`.

### Why this way

`sbt-release-early` takes a distinct approach to releases.

While some projects decide to declare versions in sbt files, `sbt-release-early`
derives the versions of your project from your git tags. This has several benefits:

1. Reproducibility.
2. Tag uniqueness. Git prevents you from trying to release a version twice.
3. Both you and the CI don't need to push commits just to bump up versions.
4. The tag history shows all the released versions over the time of a project.

These three and the automatic derivation of version reduce substantially the complexity
of handling releases. For example, implementing automatic MiMa checks is trivial --
instead of resolving latest jar or interfacing with web APIs, you can get the
latest tag from the git history.

### Version schema

Any version that is not final is considered a snapshot even if it has a stable
number and lacks the `-SNAPSHOT` suffix. For example, `0.3+10-4f489199` is a
snapshot. You could say this plugin takes the most literal definition of snapshot,
where a snapshot version is *a snapshot* of your codebase at any
time.

