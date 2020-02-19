---
title: Infrastructure project
type: docs
---

# Avito android infrastructure on github

Monorepo of all tooling to continuously test and deliver apps to users

## Modules

### Gradle plugins

To use plugins in your project:

`build.gradle`

```groovy
buildscript {
    dependencies {
        classpath("com.avito.android:instrumentation-tests:$avitoToolsVersion")   
    }
    repositories {
        jcenter()   
    }
}

apply("com.avito.android.instrumentation-tests")
```

or 

`build.gradle`

```groovy
plugins {
    id("com.avito.android.instrumentation-tests")
}
```

and in `settings.gradle`

```groovy
pluginManagement {
    repositories {
        jcenter()
    }
    resolutionStrategy {
        eachPlugin {
            String pluginId = requested.id.id
            if (pluginId.startsWith("com.avito.android")) {
                def artifact = pluginId.replace("com.avito.android.", "")
                useModule("com.avito.android:$artifact:$avitoToolsVersion")
            }
        }
    }
}
```

- `:artifactory-app-backup` - gradle plugin to back up build artifacts in [artifactory](https://jfrog.com/artifactory/)
- `:build-metrics` - gradle plugin for gathering build metrics and deliver it to [grafana](https://grafana.com/)
- `:build-properties` - gradle plugin to deliver custom build parameters to android assets
- `:buildchecks` - gradle plugin to early detection of build problems
- [`:cd`]({{< ref "/ci/CIGradlePlugin.md" >}})
- `:dependencies-lint` - gradle plugin to detect unused gradle dependencies
- `:design-screenshots` - gradle plugin, extended tasks to support screenshot testing on top of our `:instrumentation` plugin
- `:docs` - gradle plugin to automate documentation deployment (was used to deploy internally //todo remove)
- `:enforce-repos` - gradle plugin to configure dependencies repositories for internal project
- `:feature-toggles` - gradle plugin to extract feature toggles values from code and report it as build artifact
- `:impact`, `:impact-shared` - gradle plugin to search parts of the project we can avoid testing based on diff. 
- `:instrumentation-tests` - gradle plugin to set up and run instrumentation tests on android
- `:instrumentation-test-impact-analysis`, `:ui-test-bytecode-analyser` - gradle plugin to search ui tests we can avoid based on `impact-plugin` analysis
- `:kotlin-root` - gradle plugin to configure kotlin tasks for internal project
- `:lint-report` - gradle plugin merging lint reports from different modules
- `:module-types` - gradle plugin to prevent modules go to wrong configurations (android-test module as an app's implementation dependency for example) 
- `:code-ownership` - gradle plugin to prevent dependency on other team's private modules
- `:performance` - gradle plugin, extended tasks to support performance testing on top of our `:instrumentation` plugin
- `:prosector` - gradle plugin and client for security service
- `:qapps` - gradle plugin to deliver apps to internal distribution service, see [QApps]({{< ref "/cd/QApps.md" >}})
- `:robolectric`- gradle plugin to configure [robolectrtic](http://robolectric.org/) for internal project
- `:room-config` - gradle plugin to configure [room](https://developer.android.com/topic/libraries/architecture/room) for internal project
- `:signer` - gradle plugin for internal app signer

### Buildscript dependencies

- `:android` - android gradle plugin extensions, and android sdk wrapper // todo separate
- `:bitbucket` - bitbucket server client to deliver checks results right into pull request context
via [code insights](https://www.atlassian.com/blog/bitbucket/bitbucket-server-code-insights) and comments
- `:docker` - docker client to work with docker daemon from gradle
- `:files` - utils to work with files and directories
- `:git` - git client to work within gradle
See [impact analysis]({{< ref "/ci/ImpactAnalysis.md" >}})
- `:kotlin-dsl-support` - gradle api extensions //todo rename
- `:kubernetes` - kubernetes credentials config extension
- `:logging` - custom logger to serialize for gradle workers //todo no longer a problem, remove
- `:pre-build` - extensions to add tasks to the early stages of build
- `:process` - utils to execute external commands from gradle
- `:runner:client`, `:runner:service`, `:runner:shared`, `:runner:shared-test` - instrumentation tests runner
- `:sentry-config` - [sentry](https://sentry.io/) client config extension
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

### Release to jcenter

[Bintray project](https://bintray.com/avito-tech/maven/avito-android), mirroring to jcenter

1. Make sure integration tests passed via `CI integration tests against avito`
1. Make sure new project version specified in develop head
1. Manually run [Teamcity configuration (internal)](http://links.k.avito.ru/releaseAvitoTools)
1. Use new version in `avito`
1. Create PR with new `infraVersion`
1. Create release on [releases page](https://github.com/avito-tech/avito-android/releases) 

### Local integration tests against avito

1. Choose project version that will not clash with released ones (example: `2020.2.4-<yourname>-1`)
1. Run `./gradlew publishToMavenLocal -PprojectVersion=<Your test version>`
1. Run integration tests of your choice in avito with specified test version

### CI integration tests against avito

1. Run [Teamcity configuration (internal)](http://links.k.avito.ru/fastCheck) to check pull request builds. 
1. And/or [This one](http://links.k.avito.ru/fullCheck) to check full set of checks.
1. You don't need to be bothered about versions here, checks of avito would run against generated version of tools project.

{{< hint info>}}
You can also change build branch if you need to test unmerged code.
But be careful, Teamcity is tricky about this one:
 
- By default build will use develop from github agains develop from avito
- If you pick a different branch of avito, it will run against develop on github
- If you pick a different branch of github, it will run against develop on avito
- (UNTESTED) To build both projects of special branch, they should have the same name

{{< /hint >}}

