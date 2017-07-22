# Ship Scala software now!
[![Join the chat at https://gitter.im/scalacenter/sbt-release-early](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/scalacenter/sbt-release-early?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://platform-ci.scala-lang.org/api/badges/scalacenter/sbt-release-early/status.svg)](https://platform-ci.scala-lang.org/scalacenter/sbt-release-early)
[![Maven Central](https://img.shields.io/maven-central/v/ch.epfl.scala/sbt-release-early.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22sbt-release-early%22)
[![Bintray Download](https://api.bintray.com/packages/scalacenter/sbt-maven-releases/sbt-release-early/images/download.svg) ](https://bintray.com/scalacenter/sbt-maven-releases/sbt-release-early/_latestVersion)

![rickandmorty_cc_001_pt2_v83js-02](https://user-images.githubusercontent.com/2462974/27400541-aa2e8cd6-56c0-11e7-8361-08dec76e1163.jpg)

> **Thou shalt not waste time cutting releases!**
> 
> — [Rick Sanchez](rick)

## Meet `sbt-release-early`

`sbt-release-early` is an sbt plugin to follow the maxim
*"Release early, release often"*. It provides an automatic way to release
artifacts on merge and via git tags, and automates all the necessary steps to
make releasing easy.

Goals:
1. Maintainers cut and handle releases in a breeze.
1. Users do not need to beg maintainers to release.
1. Open-source contributors have access to their changes as soon as their PRs are merged.

## Read the docs

The docs are placed in a [detailed GitHub wiki](https://github.com/scalacenter/sbt-release-early/wiki).

It explains how to:
* Install and use the plugin.
* Set up your CI and infrastructure.
* Release with ease for different backends and CIs.

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

## Contributing

We welcome contributions and we actively maintain this plugin.

We recognise the following ways to contribute to this project:
* Submitting a pull request to fix anything.
* Filing a bug report in the issue tracker.
* Participating in the discussions in the Gitter channel, issues or pull requests.

Maintainers of this plugin are happy to mentor / help open-source contributors to get familiar with
sbt or improve their Scala skills. We both focus in improving this piece of
software and helping you sharpen your open-source Scala skills.

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
