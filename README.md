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

`sbt-release-early` is an sbt plugin to follow the maxim *"Release early, release often"*. It
provides an automatic way to release artifacts on merge and via git tags, and automates all the
necessary steps to make releasing easy.

#### Goals:

1. Maintainers cut and handle releases in a breeze.
1. Users do not need to beg maintainers to release.
1. Open-source contributors have access to their changes as soon as their PRs are merged.
1. Maintainers can easily change the publisher they use (Bintray or Sonatype) and their CI setup.

## Read the docs

The docs are placed in a [detailed GitHub wiki](https://github.com/scalacenter/sbt-release-early/wiki).
They have been carefully written to contain all the information you may need to publish any package
in sbt. If something is missing, please contribute to them, the wiki is open to external editions.

They explain:

* The philosophy.
* How to install, use and configure the plugin.
* How to set up your CI, gpg and related infrastructure.
* How to release for different backends and CIs.

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
open-source Scala community or the [Scala Center](https://scala.epfl.ch).
If you're a company, we encourage you to become an [Scala Center's Advisory Board member](scalacenter)
and have a say in our projects and the future of the Scala language.

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
