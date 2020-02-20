---
title: Lint Worker Api Workaround
type: docs
---

# Workaround for android lint not using worker api 

[Issue #145235363](https://issuetracker.google.com/issues/145235363)

## Problem

We have a lot of modules in project, and things builds in parallel very well, however there is a bottleneck in CI builds:
android lint and ui-tests, both on final application module.\
We use workers to parallelize different ui-testing tasks and enable workers api for available android gradle plugin tasks.\
Android lint does not use Gradle Worker API yet and blocks any of these optimizations on most occasions.

It is so, because Gradle [holds module(project) lock](https://github.com/gradle/gradle/issues/8630#issuecomment-488161594),
and even if your task use workers, it should acquire lock in order to start or report finish.

So it will parallelize as nice as you might expect only if all tasks running on module use workers, or non-worker task is fast enough to even bother.

Lint is usually LONG.

## Solution

We ended up with kinda dirty and in-theory unstable hack, but it works well.

Let's take a look only on final application module tasks:

- Long tasks supporting workers API (good tasks) must start as soon as all its dependencies ready, it will release lock right after start
- Android Lint (bad task) should start right after it, holding lock until end
- Even if good tasks finished before lint we end whole build in much more optimal time than consequentially

{{< hint info>}}
Good tasks will report finish time bigger than actual in that case, it's a [known issue](https://github.com/gradle/gradle/issues/8630#issuecomment-488161594)
{{< /hint >}}

## Implementation

We have introduced `preInstrumentation` task for ui-tests, that depends on same tasks as instrumentation one's,
but doing nothing other than `mustRunAfter` point to lint task, moving it next to ui-tests.
