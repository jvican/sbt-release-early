# Meet `sbt-release-early`
[![Join the chat at https://gitter.im/scalacenter/sbt-release-early](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/scalacenter/sbt-release-early?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://platform-ci.scala-lang.org/api/badges/scalacenter/sbt-release-early/status.svg)](https://platform-ci.scala-lang.org/scalacenter/sbt-release-early)
[![Bintray Download](https://api.bintray.com/packages/scalacenter/sbt-maven-releases/sbt-release-early/images/download.svg) ](https://bintray.com/scalacenter/sbt-maven-releases/sbt-release-early/_latestVersion)

![rickandmorty_cc_001_pt2_v83js-02](https://user-images.githubusercontent.com/2462974/27400541-aa2e8cd6-56c0-11e7-8361-08dec76e1163.jpg)

> **Thou salt not wait for a release!**
> 
> — [Rick Sanchez](rick)

`sbt-release-early` is an sbt plugin to follow the maxim
`"Release early, release often"`. The major goal is to provide out-of-the-box
support for automatic releases.

## Use

Add the latest version of this library to your project with the following sbt line:

```scala
libraryDependencies += "ch.epfl.scala" % "sbt-release-early" % "0.1.0"
```

#### Non-stable releases

Non-stable releases are freshly released from master on merge using
`sbt-release-early`! To depend on them, add the Scala Center's Bintray resolver
to your sbt project:

```scala
resolvers += Resolver.bintrayRepo("scalacenter", "sbt-maven-releases")
```

## Dependencies

This plugin relies on the following sbt plugins:

* [`sbt-dynver`](sbtdynver), version `0.2.0`.
* [`sbt-pgp`](sbtpgp), version `1.0.0`.
* [`sbt-bintray`](sbtbintray), version `0.3.0`.

If you already depend on them, remove them from your `plugins.sbt` file.

## Requirements

If you want to use `sbt-release-early`, you need to:
  
* Use git and have it in the `PATH` (required by sbt-dynver).
* Have a [Bintray](bintray) account.

To synchronize your artifacts with Maven Central, you need to:
* Have a [Sonatype](sonatype) account.
* Have a GPG key that **is published to [pgp.mit.edu](http://pgp.mit.edu/)**.

## The plugin in a nutshell

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
2. Tag uniqueness. Git prevents you from trying to release a version twice by mistake.
3. No need to push commits to bump up the versions of your projects.
4. The tag history shows all the released versions over the time of a project.

These properties reduce the complexity of handling releases significantly.
The repository revision history becomes the ultimate truth and build-related
tasks are easy to set up -- no need to resolve the latest version or scrap
the library version from its sbt build.

### Version schema

Any version that is not final is considered a snapshot even if it has a stable
number and lacks the `-SNAPSHOT` suffix. For example, `0.3+10-4f489199` is a
snapshot. This plugin takes the most literal definition of snapshot,
where a snapshot release mirrors your codebase at given point in time.

#### Why not SNAPSHOTs?

Snapshots have important shortcomings:

1. They are not reproducible: downstream users can get different snapshot releases
depending on the time they resolve/update them.
2. Resolution times go up significantly.

Their use in the open-source community is not justified when tools like [sbt-dynver](sbtdynver)
solve derive versions for your software based on invariants of your version control system.

By using automatically derived versions, `sbt-release-early` keeps your builds reproducible
and your resolution times fast.

## Plugin customization

### Synchronization with Maven Central

This feature is optional, but **enabled by default for releases via tag**.
To disable it, scope the following key in your project or globally:
```scala
releaseEarlyEnableSyncToMaven := false
```

#### Set up your Sonatype credentials

`sbt-release-early` has to pick up your Sonatype credentials from your CI environment.
Expose them via the following environment variables:

1. Sonatype username: `SONATYPE_USER` or `SONA_USER`.
1. Sonatype password: `SONATYPE_PASSWORD` or `SONA_PASS`.

#### What do I need to synchronize with Maven Central?

1. You need to create a **Maven** Bintray repository.
![bintray_maven](https://user-images.githubusercontent.com/2462974/27399758-21607d3a-56be-11e7-919d-06b9315e22ee.png)
1. When you've done your first non-stable release, you need to synchronize
your Bintray package with **JCenter**.
![jcenter-to-link](https://user-images.githubusercontent.com/2462974/27399894-9527a72a-56be-11e7-944f-f27e73d5c09f.png)

The Bintray team will accept your package into the JCenter repository within 1 or 2 hours.
You will know that you're ready to publish a stable release when you see the following in
your bintray package interface.

![jcenter](https://user-images.githubusercontent.com/2462974/27399886-89e42258-56be-11e7-8608-796a71b1db0d.png)

Then, you're ready to go. Next time you push a git tag to the CI, `releaseEarly`
will also sync your artifacts with Maven Central for free.


[sbtdynver]: https://github.com/dwijnand/sbt-dynver
[sbtpgp]: https://github.com/sbt/sbt-pgp
[sbtbintray]: https://github.com/sbt/sbt-bintray
[bintray]: https://github.com/sbt/sbt-bintray
[sonatype]: https://www.sonatype.com/
[rick]: https://www.google.ch/url?sa=t&rct=j&q=&esrc=s&source=web&cd=2&cad=rja&uact=8&ved=0ahUKEwilmJf3yc_UAhVFvhQKHVO2DwgQFgg3MAE&url=https%3A%2F%2Fen.wikipedia.org%2Fwiki%2FRick_Sanchez_(Rick_and_Morty)&usg=AFQjCNEalPWcD1EFtXjxxghoVHIAo4gy1Q

