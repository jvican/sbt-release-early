# Release early

![rocket-gif-release-early](https://cloud.githubusercontent.com/assets/2462974/24923810/06c14db8-1ef3-11e7-8ad9-19b4dc614d05.gif)

> "Release early, release often" - Eric S. Raymond

`sbt-release-early` is a sbt plugin with two goals:
  
1. Make it simpler for you to release.
2. Encourage you to cut artifacts often.

## Use

Not ready yet. Coming soon.

## Howto

This plugin assumes:
  
* Your CI is Drone.
* Your version control system is git.
* You don't need fancy control over the release process.
  
In exchange, it cuts artifacts for:
  
* Any commit that hits master (on-merge releases).
* Any git tag that you push.
