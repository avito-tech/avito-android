---
title: Infrastructure project
type: docs
---

# Avito android infrastructure on github

Monorepo of all tooling to continuously test and deliver apps to users

## Modules

### Gradle modules

Gradle plugins and buildscript dependencies.

- `:android` - android gradle plugin extensions, and android sdk wrapper // todo separate
- `:artifactory` - gradle plugin to back up build artifacts in [artifactory](https://jfrog.com/artifactory/)
- `:bitbucket` - bitbucket server client to deliver checks results right into pull request context
via [code insights](https://www.atlassian.com/blog/bitbucket/bitbucket-server-code-insights) and comments
- `:build-checks` - gradle plugin to early detection of build problems
- `:build-metrics` - gradle plugin for gathering build metrics and deliver it to [grafana](https://grafana.com/)
- `:build-properties` - gradle plugin to deliver custom build parameters to android assets
- [`:cicd`]({{< ref "/ci/CIGradlePlugin.md" >}})
- `:dependencies-lint` - gradle plugin to detect unused gradle dependencies
- `:design-screenshots` - gradle plugin, extended tasks to support screenshot testing on top of our `:instrumentation` plugin
- `:docker` - docker client to work with docker daemon from gradle
- `:docs-deployer` - gradle plugin to automate documentation deployment (was used to deploy internally //todo remove)
- `:enforce-repos` - gradle plugin to configure dependencies repositories for internal project
- `:feature-toggle-report` - gradle plugin to extract feature toggles values from code and report it as build artifact
- `:files` - utils to work with files and directories
- `:git` - git client to work within gradle
- `:impact`, `:impact-plugin` - gradle plugin to search parts of the project we can avoid testing based on diff. 
See [impact analysis]({{< ref "/ci/ImpactAnalysis.md" >}})
- `:instrumentation` - gradle plugin to set up and run instrumentation tests on android
- `:instrumentation-impact-analysis`, `:ui-test-bytecode-analyser` - gradle plugin to search ui tests we can avoid based on `impact-plugin` analysis
- `:kotlin-config` - gradle plugin to configure kotlin tasks for internal project
- `:kotlin-dsl-support` - gradle api extensions //todo rename
- `:kubernetes` - kubernetes credentials config extension
- `:lint-report` - gradle plugin merging lint reports from different modules
- `:logging` - custom logger to serialize for gradle workers //todo no longer a problem, remove
- `:module-type` - gradle plugin to prevent modules go to wrong configurations (android-test module as an app's implementation dependency for example) 
- `:ownership` - gradle plugin to prevent dependency on other team's private modules
- `:performance` - gradle plugin, extended tasks to support performance testing on top of our `:instrumentation` plugin
- `:pre-build` - extensions to add tasks to the early stages of build
- `:process` - utils to execute external commands from gradle
- `:prosector` - gradle plugin and client for security service
- `:qapps` - gradle plugin to deliver apps to internal distribution service, see [QApps]({{< ref "/cd/QApps.md" >}})
- `:robolectric-config`- gradle plugin to configure [robolectrtic](http://robolectric.org/) for internal project
- `:room-config` - gradle plugin to configure [room](https://developer.android.com/topic/libraries/architecture/room) for internal project
- `:runner:client`, `:runner:service`, `:runner:shared`, `:runner:shared-test` - instrumentation tests runner
- `:sentry-config` - [sentry](https://sentry.io/) client config extension
- `:signer` - gradle plugin for internal app signer
- `:slack` - [slack](https://slack.com/) client to work within gradle plugins
- `:statsd-config` - [statsd](https://github.com/statsd/statsd) client config extension
- `:teamcity` - wrapper for [teamcity](https://www.jetbrains.com/ru-ru/teamcity/) [client](https://github.com/JetBrains/teamcity-rest-client)
and [service messages]((https://www.jetbrains.com/help/teamcity/build-script-interaction-with-teamcity.html#BuildScriptInteractionwithTeamCity-ServiceMessages))
- `:test-project` - [Gradle Test Kit](https://docs.gradle.org/current/userguide/test_kit.html) project generator and utilities
- `:test-summary` - test suite summary writer
- `:trace-event` - client for [trace event format](https://docs.google.com/document/d/1CvAClvFfyA5R-PhYUmn5OOQtYMH4h6I0nSsKchNAySU/preview)
- `:upload-cd-build-result` - client for internal "Apps release dashboard" service
- `:upload-to-googleplay` - wrapper for google publishing api
- `:utils` - //todo remove 

### Android-test modules

Code that goes in androidTestImplementation configuration and runs on emulators.

- `:junit-utils` - //todo move to common
- `:mockito-utils` - //todo move to common
- `:resource-manager-exceptions` - //todo remove
- `:test-annotations` - annotations to supply meta information for reports and [test management system]({{< ref "/test/TestManagementSystem.md" >}})
- `:test-app` - app we are using to test `:ui-testing-` libraries
- `:test-inhouse-runner` - custom [android junit runner](https://developer.android.com/reference/android/support/test/runner/AndroidJUnitRunner.html)
- `:test-report` - client to gather test runtime information for reporting
- `:ui-testing-core` - main ui testing library, based on [espresso](https://developer.android.com/training/testing/espresso)
- `:ui-testing-maps` - addon for main library to test google maps scenarios
- `:websocket-reporter` - client to gather websocket info for reporting

### Common modules

Shared modules between android-test and gradle.

- `:file-storage` - client for internal file storage client, used to store screenshots, videos and other binary stuff
- `:okhttp` - okhttp extensions
- `:sentry` - [sentry]((https://sentry.io/)) client
- `:statsd` - [statsd]((https://github.com/statsd/statsd)) client
- `:test-okhttp` - wrapper for [okhttpmockwebserver](https://github.com/square/okhttp/tree/master/mockwebserver)
- `:time` - simple time api 

## Publishing

1. [Bintray](https://bintray.com/avito-tech/maven/avito-android), mirroring to jcenter
1. [Inhouse artifactory](http://links.k.avito.ru/androidArtifactory) to check staging version in integration with internal repo

### Teamcity

[Internal project](http://links.k.avito.ru/androidTeamcity)

Bintray publish uses: `./publish.sh`
Artifactory publish uses: `./publish_local.sh`

### Gradle

`./gradlew publishRelease`

environmental argument should be set:

- `BINTRAY_USER`
- `BINTRAY_API_KEY`

gradle properties should be set:

- `gradle.publish.key`
- `gradle.publish.secret`

`./gradlew publishToArtifactory`

environmental argument should be set:

- `ARTIFACTORY_URL`
- `ARTIFACTORY_USER`
- `ARTIFACTORY_PASSWORD`

To specify a version, use `PROJECT_VERSION` env or `projectVersion` gradle property 
