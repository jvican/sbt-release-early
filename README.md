# Release early with sbt

![zwukqyioyvrqu](https://cloud.githubusercontent.com/assets/2462974/24969890/a3e68c24-1fb2-11e7-823b-dea3ff4aeaac.gif)

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
