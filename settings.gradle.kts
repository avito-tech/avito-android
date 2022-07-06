enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "avito-android-infra"

if (gradle.startParameter.isOffline) {
    logger.warn(
        "You are in offline mode." +
            " If you have issues with dependency resolution" +
            " you should run Gradle task without offline mode for cache dependency locally"
    )
}

pluginManagement {
    // See rationale inside these scripts
    apply(from = "build-logic-settings/scan-plugin/buildScan-disableAutoApplyFix.settings.gradle.kts")
    apply(from = "build-logic-settings/dependency-plugin/pluginManagement-shared.settings.gradle.kts")

    @Suppress("UnstableApiUsage")
    includeBuild("build-logic-settings")
}

plugins {
    id("convention-scan")
    id("convention-cache")
    id("convention-dependencies")
}

includeBuild("build-logic")

include(":subprojects:gradle:android")
include(":subprojects:gradle:bitbucket")
include(":subprojects:gradle:build-environment")
include(":subprojects:gradle:build-failer")
include(":subprojects:gradle:code-ownership:api")
include(":subprojects:gradle:code-ownership:plugin")
include(":subprojects:gradle:deeplink-generator")
include(":subprojects:gradle:design-screenshots")
include(":subprojects:gradle:git")
include(":subprojects:gradle:gradle-extensions")
include(":subprojects:gradle:graphite-config")
include(":subprojects:gradle:impact")
include(":subprojects:gradle:impact-shared")
include(":subprojects:gradle:module-dependencies-graph")
include(":subprojects:gradle:module-dependencies")
include(":subprojects:gradle:module-types")
include(":subprojects:gradle:pre-build")
include(":subprojects:gradle:process")
include(":subprojects:gradle:prosector")
include(":subprojects:gradle:slack")
include(":subprojects:gradle:statsd-config")
include(":subprojects:gradle:teamcity")
include(":subprojects:gradle:test-project")
include(":subprojects:gradle:test-summary")
include(":subprojects:gradle:ui-test-bytecode-analyzer")
include(":subprojects:gradle:worker-extensions")

include(":subprojects:assemble:build-checks")
include(":subprojects:assemble:build-metrics")
include(":subprojects:assemble:build-metrics-tracker")
include(":subprojects:assemble:build-properties")
include(":subprojects:assemble:build-trace")
include(":subprojects:assemble:build-verdict")
include(":subprojects:assemble:build-verdict-tasks-api")
include(":subprojects:assemble:critical-path:api")
include(":subprojects:assemble:critical-path:critical-path")
include(":subprojects:assemble:gradle-profile")

include(":subprojects:common:build-metadata")
include(":subprojects:common:resources")
include(":subprojects:common:files")
include(":subprojects:common:time")
include(":subprojects:common:okhttp")
include(":subprojects:common:test-okhttp")
include(":subprojects:common:result")
include(":subprojects:common:elastic")
include(":subprojects:common:http-client")
include(":subprojects:common:sentry")
include(":subprojects:common:graphite")
include(":subprojects:common:statsd")
include(":subprojects:common:statsd-test-fixtures")
include(":subprojects:common:problem")
include(":subprojects:common:waiter")
include(":subprojects:common:kotlin-ast-parser")
include(":subprojects:common:random-utils")
include(":subprojects:common:teamcity-common")
include(":subprojects:common:junit-utils")
include(":subprojects:common:graph")
include(":subprojects:common:math")
include(":subprojects:common:retrace")
include(":subprojects:common:reflection-extensions")
include(":subprojects:common:trace-event")
include(":subprojects:common:truth-extensions")
include(":subprojects:common:composite-exception")
include(":subprojects:common:throwable-utils")
include(":subprojects:common:coroutines-extension")
include(":subprojects:common:command-line")
include(":subprojects:common:command-line-rx")
include(":subprojects:common:command-line-coroutines")

include(":subprojects:android-test:resource-manager-exceptions")
include(":subprojects:android-test:websocket-reporter")
include(":subprojects:android-test:keep-for-testing")
include(":subprojects:android-test:ui-testing-core-app")
include(":subprojects:android-test:ui-testing-core")
include(":subprojects:android-test:instrumentation")
include(":subprojects:android-test:toast-rule")
include(":subprojects:android-test:snackbar-rule")
include(":subprojects:android-test:test-screenshot")
include(":subprojects:android-test:rx3-idler")

include(":subprojects:android-lib:proxy-toast")
include(":subprojects:android-lib:snackbar-proxy")

include(":subprojects:test-runner:test-report-artifacts")
include(":subprojects:test-runner:test-annotations")
include(":subprojects:test-runner:test-report-api")
include(":subprojects:test-runner:test-report-dsl-api")
include(":subprojects:test-runner:test-report-dsl")
include(":subprojects:test-runner:test-report")
include(":subprojects:test-runner:test-report-android")
include(":subprojects:test-runner:test-report-jvm")
include(":subprojects:test-runner:test-report-run-listener")
include(":subprojects:test-runner:test-inhouse-runner")
include(":subprojects:test-runner:test-instrumentation-runner")
include(":subprojects:test-runner:robolectric-inhouse-runner")
include(":subprojects:test-runner:shared:logger-providers")
include(":subprojects:test-runner:shared-tests")
include(":subprojects:test-runner:k8s-deployments-cleaner")
include(":subprojects:test-runner:instrumentation-changed-tests-finder")
include(":subprojects:test-runner:instrumentation-tests")
include(":subprojects:test-runner:instrumentation-tests-dex-loader")
include(":subprojects:test-runner:file-storage")
include(":subprojects:test-runner:report-viewer")
include(":subprojects:test-runner:report-processor")
include(":subprojects:test-runner:report")
include(":subprojects:test-runner:test-model")
include(":subprojects:test-runner:runner-api")
include(":subprojects:test-runner:client")
include(":subprojects:test-runner:device-provider:model")
include(":subprojects:test-runner:device-provider:impl")
include(":subprojects:test-runner:device-provider:api")
include(":subprojects:test-runner:service")
include(":subprojects:test-runner:kubernetes")
include(":subprojects:test-runner:transport")
include(":subprojects:test-runner:plugins-configuration")

include(":subprojects:logger:gradle-logger")
include(":subprojects:logger:logger")
include(":subprojects:logger:sentry-logger")
include(":subprojects:logger:elastic-logger")
include(":subprojects:logger:android-logger")
include(":subprojects:logger:slf4j-gradle-logger")

include(":subprojects:delivery:artifactory-app-backup")
include(":subprojects:delivery:nupokati")
include(":subprojects:delivery:cd")
include(":subprojects:delivery:qapps")
include(":subprojects:delivery:sign-service")
include(":subprojects:delivery:legacy-signer")
include(":subprojects:delivery:tests-summary")
include(":subprojects:delivery:upload-cd-build-result")
include(":subprojects:delivery:upload-to-googleplay")

include(":subprojects:emcee:gradle-plugin")
include(":subprojects:emcee:queue-client-api")
include(":subprojects:emcee:client")
include(":subprojects:emcee:queue-client-models")
include(":subprojects:emcee:queue-api-models")
include(":subprojects:emcee:queue-worker-api")
include(":subprojects:emcee:worker")
include(":subprojects:emcee:android-device")
