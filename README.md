# Release early

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
