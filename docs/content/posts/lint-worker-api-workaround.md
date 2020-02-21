---
date: 2020-02-20
title: Android lint and Gradle worker API workaround
tags: [gradle, agp, lint]
weight: 10
---

# Workaround for Android lint that doesn't use Gradle Worker API 

[Issue #145235363](https://issuetracker.google.com/issues/145235363)

## Problem

We have a lot of modules in the project, and things build in parallel very well. 
However, there is a contention between Android lint and UI-tests in CI builds. Both tasks are on a final application module.\
We use workers to parallelize different UI-testing tasks and enable Gradle workers API for available android Gradle plugin tasks.\
Android lint does not use Gradle Worker API yet and blocks any of these optimizations on most occasions.

It is so, because Gradle [holds module (project) lock](https://github.com/gradle/gradle/issues/8630#issuecomment-488161594),
and even if your task use workers, it should acquire the lock to start or report finish.

So it will parallelize as nice as you might expect only if all tasks running on module use workers, or non-worker task is fast enough to even bother.

{{< hint info>}}
Tasks report finish time later than actual: [gradle #8630](https://github.com/gradle/gradle/issues/8630#issuecomment-488161594)
{{< /hint >}}

{{<mermaid>}}
gantt
	title Lint blocks instrumentation to start
	dateFormat      YYYY-MM-DD
	axisFormat      %j
	section         project
	package         :package, 1000-01-01, 11d
	lint            :lint, 1000-01-02, 10d
	instrumentation :after lint package, 10d
{{</mermaid>}}				

## Solution

We ended up with a kinda dirty and in-theory unstable hack, but it works well.

Let's take a look only on final application module tasks:

- Long tasks supporting workers API (good tasks) must start as soon as all its dependencies ready. It will release a lock right after the start
- Android lint (bad task) should start right after it, holding a lock until the end
- Even if good tasks finished before lint we end a whole build in much more optimal time than consequentially

{{<mermaid>}}
gantt
	title Lint runs in parallel with instrumentation
	dateFormat      YYYY-MM-DD
	axisFormat      %j
	section         project
	package         :package, 1000-01-01, 3d
	instrumentation :after package, 11d
	lint            :lint, 1000-01-05, 10d
{{</mermaid>}}

## Implementation

We have [introduced](https://github.com/avito-tech/avito-android/pull/200) `preInstrumentation` task for UI-tests, that depends on the same tasks as instrumentation one's,
but doing nothing other than `mustRunAfter` point to lint task, moving it next to UI-tests.
