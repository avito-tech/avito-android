---
title: buildSrc in Gradle
type: docs
---

# buildSrc in Gradle

**Quadrant:** tools\
**Status (ring)**: hold

It's a default and convenient way for organizing custom plugins and tasks: 
[Gradle - build sources](https://docs.gradle.org/current/userguide/organizing_gradle_projects.html#sec:build_sources).

## Pros

No versions. It works as implicitly included build.

## Cons

### Slows project synchronization in IDE

At Avito, we had 6% of Kotlin code in buildSrc but it cost us about 40% of the time even with all possible optimizations.

### Lack of support in IDEA

Workaround: work with buildSrc as a standalone project.

### Struggle for reusing Gradle and Kotlin daemons

As a result you waste an extra RAM.\
Example for Gradle daemon: [gradle/buildSrc#checkSameDaemonArgs](https://github.com/gradle/gradle/blob/master/buildSrc/build.gradle.kts#L175)

### Slows configuration time

Workaround:

- Use remote build cache and keep track a hit rate
- Exclude compilation of tests in buildSrc in local builds
- Run tests in buildSrc only in CI

### Can't reuse a module from buildSrc

Technically, you can point directly to a jar from any buildSrc module. 
This way has limitations: 

- IDE knows nothing about it and shows "Unsupported module" warning
- Requires buildSrc recompilation after changes
- It's hard to support transitive dependencies

### Harmful for compile avoidance

Compiled classes from buildSrc will be added to a build script classpath. 
It can cause extra cache misses and recompilation after changes.

## Recommendations

Certainly it's a better place for custom plugins, tasks, logic than build-scripts.
If you have loads of code or heavy integration tests in plugins, consider moving them to a standalone project.
