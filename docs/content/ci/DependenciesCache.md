---
title: Dependencies cache
type: docs
---

# Dependencies cache

## Problem

Building with gradle in containers could be very inefficient due to re-downloading wrapper, dependencies and build cache every time.

## Writeable volume

Mounting writeable `.gradle/caches` is an ok solution, if you can guarantee non-concurrent access.\
In real world though previous container can crash unpredictably, with lock files left.\
Also you usually want image to be locally testable, and track ongoing gradle processes during development phase brings even more trouble.

You have to make sure and clear all leftovers before starting new container.

## Read-only cache

Gradle 6.2 brings new way to handle this problem: [read-only dependencies cache](https://docs.gradle.org/current/userguide/dependency_resolution.html#sub:ephemeral-ci-cache)\
It's much cleaner from within container side: read only cache mounted in separate folder and if cache is not full,
new dependencies will be written in `.gradle/caches` which is now a delta we could use to decide if we need cache warm-up.

## Writing cache

Warming up cache left to us. We use simple solution for now: sharing the cache only between containers on same teamcity-agent-node.\
Our nodes is a cloud containers themselves, and lives though work day, so cache will be re-downloaded every morning.

- Every build, before container run looking for a marker file in host machine's `.gradle/caches`
- If marker found(means cache is available), read only cache mounted
- If marker not found, writeable cache mounted and populated during build, leaving marker after

## Future optimizations

- Share cache between all build agents (maybe via rsync) and lives forever.
- Single build will be triggered to warm-up cache after any build reporting of non-empty delta.
