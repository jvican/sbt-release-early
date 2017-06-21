# Meet `sbt-release-early`
[![Join the chat at https://gitter.im/scalacenter/sbt-release-early](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/scalacenter/sbt-release-early?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://platform-ci.scala-lang.org/api/badges/scalacenter/sbt-release-early/status.svg)](https://platform-ci.scala-lang.org/scalacenter/sbt-release-early)
[![Maven Central](https://img.shields.io/maven-central/v/ch.epfl.scala/sbt-release-early.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22sbt-release-early%22)
[![Bintray Download](https://api.bintray.com/packages/scalacenter/sbt-maven-releases/sbt-release-early/images/download.svg) ](https://bintray.com/scalacenter/sbt-maven-releases/sbt-release-early/_latestVersion)

![rickandmorty_cc_001_pt2_v83js-02](https://user-images.githubusercontent.com/2462974/27400541-aa2e8cd6-56c0-11e7-8361-08dec76e1163.jpg)

> **Thou shalt not waste time to cut releases!**
> 
> — [Rick Sanchez](rick)

`sbt-release-early` is an sbt plugin to follow the maxim
`"Release early, release often"`. It provides an automatic way to release
artifacts on merge and via git tags, as well as some nice features to make
the release process less cumbersome.

Goals:
1. Maintainers cut and handle releases in a breeze.
1. Users do not need to beg maintainers to release.
1. Open-source contributors have access to their changes as soon as their PRs are merged.

## Installation

Add the latest version of this library to your project with the following sbt line:

```scala
libraryDependencies += "ch.epfl.scala" % "sbt-release-early" % "0.1.0"
```

#### Add Scala Center's bintray resolver

Our Bintray resolver is useful to depend on non-final releases or final releases of
`sbt-release-early` that are on their way to Maven Central. To add this resolver to
your build, drop the following line in your build:

```scala
resolvers in Global += Resolver.bintrayRepo("scalacenter", "sbt-maven-releases")
```

## Requirements

If you want to use `sbt-release-early`, you need to:
  
* Use git and have it in the `PATH` (required by sbt-dynver).
* Have a [Bintray](bintray) account.

To synchronize your artifacts with Maven Central, you need to:
* Have a [Sonatype](sonatype) account.
* Have a GPG key that **is published to [pgp.mit.edu](http://pgp.mit.edu/)**.
* [Set up Bintray for it](#synchronization-with-maven-central).

## How to use it

1. Make sure you fulfill all the [requirements](#requirements).
1. Make sure your build and CI are [correctly set up](#configure-your-release).
1. Run `sbt releaseEarly` in your CI.

## Dependencies

This plugin relies on the following sbt plugins:

* [`sbt-dynver`](sbtdynver), version `1.3.0`.
* [`sbt-pgp`](sbtpgp), version `1.0.0`.
* [`sbt-bintray`](sbtbintray), version `0.5.0`.

If you already depend on them, remove them from your `plugins.sbt` file.

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
2. Tag uniqueness. Git prevents you from trying to release a version twice by mistake.*
3. No need to push commits to bump up the versions of your projects.
4. The tag history shows all the released versions over the time of a project.

These properties reduce the complexity of handling releases significantly.
The repository revision history becomes the ultimate truth and build-related
tasks are easy to set up -- no need to resolve the latest version or scrap
the library version from its sbt build.

\* This assumes you won't ever delete a an already pushed tag. Don't make life more
dangerous that what it already is.

### Version schema

The version lingo is extracted from the [Semantic Versioning document](semver).

This documentation and this plugin assumes that:
* Version numbers are **final** if they **only** consist of a MAJOR, MINOR and PATCH VERSION.
  For example, `1.0.1`, `2.4.98` and `10.1.6` are final releases.
* Any other version number that contains build metadata or qualifiers like `RC`,
  `beta` or `alpha` are considered SNAPSHOTs even if they lack the `-SNAPSHOT`
  suffix. For example, `0.3.0+10-4f489199`, `8.9.1-1f43aa21` or `1.0.0-RC` are snapshots.

Note that the snapshot does not have a precise and commonly accepted definition.
`sbt-release-early`s definition differs from the common understanding of snapshot
versions in the Scala community.

`sbt-release-early` considers the most literal definition of snapshot: it's a release
that mirrors the codebase at a given point of time and whose artifacts could not provide
the same guarantees as final releases. These guarantees are established by library authors.

#### Why not regular `-SNAPSHOT`s?

Snapshots have important shortcomings:

1. They are **not reproducible**: downstream users can get different snapshot releases
depending on the time they resolve/update them.
2. Their **resolution times are slower**: build tools have to check all artifacts in all resolvers to choose
the appropriate version.

The use of snapshots is no longer justified with `sbt-release-early` and sbt plugins like
[sbt-dynver](sbtdynver) that derive automatic versions from git invariants.
`sbt-release-early` keeps your builds **faster and more reproducible**.

## Configure your release

### Configure your CI and your build

Whether you use Drone or not, the following [.drone.yml](.drone.yml) can inspire you.

Your **CI** needs to provide:
1. The bintray username and password. Preferred choice is via the `BINTRAY_USERNAME`
and `BINTRAY_PASSWORD` environment variables. More in the [official docs](bintray-publishing).
2. The sonatype username and password. Either via:
    1. `SONATYPE_USER` and `SONATYPE_PASSWORD`; or
    1. `SONA_USER` or `SONA_PASS`.
3. The pgp password through an environment variable.
    
Your **build** needs to provide the following settings for every publishable module
(with `.settings(publishSettings)`) or globally (appending `in Global` to every key).
```scala
lazy val publishSettings = Seq(
  // The sbt-pgp requirements for tag-driven releases and Maven Central sync
  pgpPassphrase := sys.env.get("PGP_PASSWORD").map(_.toArray),
  pgpPublicRing := file("LOCATION_OF_YOUR_CI_PUBLIC_RING"),
  pgpSecretRing := file("LOCATION_OF_YOUR_CI_SECRET_RING"),
  // These are the **minimum** requirements to release with `sbt-release-early`
  publishMavenStyle := true,
  bintrayRepository := "your-bintray-maven-repository",
  licenses := Seq(
    // Your license, I recommend the Mozilla Public License!
    "MPL-2.0" -> url("https://opensource.org/licenses/MPL-2.0")
  ),
  homepage := Some(url("https://github.com/your-handle/your-project")),
  scmInfo := Some(ScmInfo(
      url("https://github.com/your-handle/your-project"),
      "scm:git:git@github.com:your-handle/your-project.git"
  )),
  developers := List(
    Developer("your-handle", "Your Name", "email@somewhere.me", url("https://github.com/your-handle"))
  ),
  // Nice to have: don't publish tests sources
  publishArtifact in Test := false
)
```

If you want to further customize bintray's or pgp's settings, read the docs of the dependent plugins
in [requirements](#requirements).

Once you have made sure the CI and the build are correctly set up, you only need to do
`sbt releaseEarly` **in the CI**. Note that this will fail if you do it locally.

If you want to try your setup with a random version locally, you can
define `releaseEarlyEnableLocalReleases := true` in your projects so that local releases are enabled.
This is **not recommended**.

### Secure releases


No matter what your CI is, you need to ensure that:
* You only depend on trusted sbt plugins (with final releases, i.e. no SNAPSHOTs).
* Releases only happen in branches that are write-protected and to which only the maintainers have access.
* Releases are disabled for pull requests. This prevents malicious users from adding sbt plugins or introspecting the build.
* Your CI configuration files are signed so that no one can have access to the environment variables.
* Your publish step does as less work as possible (for example, no test execution).

For instance, Drone provides an automatic way to:
* Scope environment variables to concrete pipeline steps.
* Execute pipeline steps only for concrete events or branches.
* Sign the configuration files (.drone.yml).

If your CI does not ensure any of these points, you are encouraged to give
Drone a try. It's one of the best CIs out there.

### Synchronization with Maven Central

This feature is optional, but **enabled by default for releases via tag**.
To disable it, scope the following key in your project or globally:
```scala
releaseEarlyEnableSyncToMaven := false
```

#### What do I need to synchronize with Maven Central?

1. You need to create a **Maven** Bintray repository. <p>
![bintray_maven](https://user-images.githubusercontent.com/2462974/27399758-21607d3a-56be-11e7-919d-06b9315e22ee.png)
1. When you've done your first non-final release, you need to synchronize
your Bintray package with **JCenter**. <p>
![jcenter-to-link](https://user-images.githubusercontent.com/2462974/27399894-9527a72a-56be-11e7-944f-f27e73d5c09f.png)

The Bintray team will accept your package into the JCenter repository.
This process will last one or two hours and when they accept your package you will receive
a notification and see the following section in your bintray package interface.

![jcenter](https://user-images.githubusercontent.com/2462974/27399886-89e42258-56be-11e7-8608-796a71b1db0d.png)

Then, if you provide all the required [credentials](#configure-your-ci-and-your-build),
`releaseEarly` will will sync your artifacts with Maven Central for free.

## Contributing

We welcome contributions and we actively maintain this plugin, meaning that every contribution
will get the best of our attention.

We recognise the following ways to contribute to this project:
* Submitting a pull request to fix anything.
* Filing a bug report in the issue tracker.
* Participating in the discussions in the Gitter channel, issues or pull requests.

Maintainers of this plugin are happy to mentor / help open-source contributors to get familiar with
sbt or improve their Scala skills. Our focus is on improving this plugin while helping you do open-source
work and learn.

If you like this work and have the financial means, we encourage you to either donate to the
open-source Scala community or the [Scala Center](https://scala.epfl.ch)
If you're a company, we encourage you to become an [Scala Center's Advisory Board member](scalacenter).

The interactions on this repository and its ecosystem are governed by the [Scala Code of Conduct](https://www.scala-lang.org/conduct.html).

[sbtdynver]: https://github.com/dwijnand/sbt-dynver
[sbtpgp]: https://github.com/sbt/sbt-pgp
[sbtbintray]: https://github.com/sbt/sbt-bintray
[bintray]: https://github.com/sbt/sbt-bintray
[sonatype]: https://www.sonatype.com/
[rick]: https://www.google.ch/url?sa=t&rct=j&q=&esrc=s&source=web&cd=2&cad=rja&uact=8&ved=0ahUKEwilmJf3yc_UAhVFvhQKHVO2DwgQFgg3MAE&url=https%3A%2F%2Fen.wikipedia.org%2Fwiki%2FRick_Sanchez_(Rick_and_Morty)&usg=AFQjCNEalPWcD1EFtXjxxghoVHIAo4gy1Q
[bintray-publishing]: https://github.com/sbt/sbt-bintray#publishing
[semver]: http://semver.org/
[scalacenter]: https://scala.epfl.ch/faqs.html
