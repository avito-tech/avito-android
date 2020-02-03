---
title: Test minimized build
type: docs
---

# Testing minimized build

About minimization: [link]({{< ref "/assemble/Minimization.md" >}})

We want to run our ui tests against build as close as possible to production one.\
It's quite a challenging task, mostly because of [tooling problems](https://issuetracker.google.com/issues/126429384)\
Developers should maintain a list of keep rules of code referenced from test app manually.

We work around these problems by using [keeper](https://slackhq.github.io/keeper/).

## Build type

Our build types: [link]({{< ref "/assemble/BuildTypes.md" >}})

We chose `staging` as a type to test against for now, it is based on debug, but with minimization and resource shrinking enabled.\
Staging build type used as main type for [manual testing]({{< ref "/test/Manual.md" >}}), as it offers all debug options.\
However we could miss some bugs, because debug(not release) sources included, so we should reconsider and introduce 4th type in the future.

## Sample

You can check configuration in `:test-app` module.
