---
title: Android infrastructure
type: docs
---

# Avito android infrastructure on github

Monorepo of all tooling to continuously test and deliver apps to users.

## Modules

### Gradle plugins

To use plugins in your project:

{{%plugins-setup%}}

Plugins:

- `:artifactory-app-backup` - Gradle plugin to back up build artifacts in [artifactory](https://jfrog.com/artifactory/)
- `:build-metrics` - Gradle plugin for gathering build metrics and deliver them to [grafana](https://grafana.com/)
- `:build-properties` - Gradle plugin to deliver custom build parameters to Android assets
- [`:buildchecks`]({{< ref "/docs/projects/BuildChecks.md" >}}) - Gradle plugin to early detection of build problems
- [`:cd`]({{< ref "/docs/projects/CISteps.md" >}})
- `:design-screenshots` - Gradle plugin, extended tasks to support screenshot testing on top of our `:instrumentation` plugin
- `:feature-toggles` - Gradle plugin to extract feature toggles values from code and report it as build artifact
- `:impact`, `:impact-shared` - Gradle plugin to search parts of the project we can avoid testing based on diff. 
- `:instrumentation-tests` - Gradle plugin to set up and run instrumentation tests on Android
- `:instrumentation-test-impact-analysis`, `:ui-test-bytecode-analyser` - Gradle plugin to search ui tests we can avoid based on `impact-plugin` analysis
- `:kotlin-root` - Gradle plugin to configure kotlin tasks for internal project
- `:lint-report` - Gradle plugin merging lint reports from different modules
- `:module-types` - Gradle plugin to prevent modules go to wrong configurations (android-test module as an app's implementation dependency for example) 
- `:code-ownership` - Gradle plugin to prevent dependency on other team's private modules
- `:performance` - Gradle plugin, extended tasks to support performance testing on top of our `:instrumentation` plugin
- `:prosector` - Gradle plugin and client for security service
- `:qapps` - Gradle plugin to deliver apps to internal distribution service, see [QApps]({{< ref "/docs/cd/QApps.md" >}})
- `:robolectric`- Gradle plugin to configure [robolectrtic](http://robolectric.org/) for internal project
- `:room-config` - Gradle plugin to configure [room](https://developer.android.com/topic/libraries/architecture/room) for internal project
- `:signer` - Gradle plugin for internal app signer

### Build script dependencies

- `:android` - Android Gradle plugin extensions, and Android SDK wrapper // todo separate
- `:bitbucket` - Bitbucket client to deliver checks results right into pull request context
via [code insights](https://www.atlassian.com/blog/bitbucket/bitbucket-server-code-insights) and comments
- `:docker` - docker client to work with docker daemon from Gradle
- `:files` - utils to work with files and directories
- `:git` - git client to work within Gradle
See [impact analysis]({{< ref "/docs/ci/ImpactAnalysis.md" >}})
- `:kotlin-dsl-support` - Gradle api extensions //todo rename
- `:kubernetes` - kubernetes credentials config extension
- `:logging` - custom logger to serialize for Gradle workers //todo no longer a problem, remove
- `:pre-build` - extensions to add tasks to the early stages of build
- `:process` - utils to execute external commands from Gradle
- `:runner:client`, `:runner:service`, `:runner:shared`, `:runner:shared-test` - instrumentation tests runner
- `:sentry-config` - [sentry](https://sentry.io/) client config extension
- `:slack` - [slack](https://slack.com/) client to work within Gradle plugins
- `:statsd-config` - [statsd](https://github.com/statsd/statsd) client config extension
- `:teamcity` - wrapper for [teamcity](https://www.jetbrains.com/ru-ru/teamcity/) [client](https://github.com/JetBrains/teamcity-rest-client)
and [service messages]((https://www.jetbrains.com/help/teamcity/build-script-interaction-with-teamcity.html#BuildScriptInteractionwithTeamCity-ServiceMessages))
- `:test-project` - [Gradle Test Kit](https://docs.gradle.org/current/userguide/test_kit.html) project generator and utilities
- `:test-summary` - test suite summary writer
- `:trace-event` - client for [trace event format](https://docs.google.com/document/d/1CvAClvFfyA5R-PhYUmn5OOQtYMH4h6I0nSsKchNAySU/preview)
- `:upload-cd-build-result` - client for internal "Apps release dashboard" service
- `:upload-to-googleplay` - wrapper for google publishing api
- `:utils` - //todo remove 

### Android testing modules

Code that goes in `androidTestImplementation` configuration and runs on emulators.

- `:junit-utils` - //todo move to common
- `:mockito-utils` - //todo move to common
- `:resource-manager-exceptions` - //todo remove
- `:test-annotations` - annotations to supply meta information for reports and [test management system]({{< ref "/docs/test/TestManagementSystem.md" >}})
- `:test-app` - app we are using to test `:ui-testing-` libraries
- `:test-inhouse-runner` - custom [android junit runner](https://developer.android.com/reference/android/support/test/runner/AndroidJUnitRunner.html)
- `:test-report` - client to gather test runtime information for reporting
- `:ui-testing-core` - main ui testing library, based on [espresso](https://developer.android.com/training/testing/espresso)
- `:ui-testing-maps` - addon for main library to test google maps scenarios
- `:websocket-reporter` - client to gather websocket info for reporting

### Android libraries

- [`:proxy-toast`]({{< ref "/docs/test/Toast.md" >}}) - helps with testing toasts

### Common modules

Shared modules between android-test and Gradle.

- `:file-storage` - client for internal file storage client, used to store screenshots, videos and other binary stuff
- `:okhttp` - okhttp extensions
- `:sentry` - [sentry]((https://sentry.io/)) client
- `:statsd` - [statsd]((https://github.com/statsd/statsd)) client
- `:test-okhttp` - wrapper for [okhttpmockwebserver](https://github.com/square/okhttp/tree/master/mockwebserver)
- `:time` - simple time api 
