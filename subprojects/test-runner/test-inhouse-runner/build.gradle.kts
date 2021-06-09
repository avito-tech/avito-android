plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
    id("convention.libraries")
}

dependencies {
    api(project(":test-runner:test-instrumentation-runner"))
    api(project(":common:junit-utils"))
    api(project(":test-runner:test-report-dsl-api"))
    api(libs.sentry) {
        because("InHouseInstrumentationTestRunner.sentry")
    }

    implementation(project(":common:build-metadata"))
    implementation(project(":common:sentry"))
    implementation(project(":logger:elastic-logger"))
    implementation(project(":common:http-client"))
    implementation(project(":common:okhttp"))
    implementation(project(":common:statsd"))
    implementation(project(":common:report-viewer")) {
        because("knows about avito report model: ReportCoordinates, RunId for LocalRunTrasport from test-report")
    }
    implementation(project(":test-runner:test-report-artifacts")) {
        because("uses factory to create TestArtifactsProvider")
    }
    implementation(project(":logger:logger"))
    implementation(project(":common:junit-utils"))
    implementation(project(":common:test-okhttp"))
    implementation(project(":test-runner:test-annotations"))
    implementation(project(":test-runner:file-storage"))
    implementation(project(":common:time"))
    implementation(project(":logger:android-log"))
    implementation(project(":android-test:ui-testing-core"))
    implementation(project(":android-test:ui-testing-maps"))
    implementation(project(":android-test:instrumentation"))
    implementation(project(":test-runner:test-report"))
    implementation(libs.androidXTestRunner)
    implementation(libs.truth)
    implementation(libs.mockitoKotlin)
    implementation(libs.okhttpLogging)
    implementation(libs.okhttpMockWebServer)
    implementation(libs.gson)

    testImplementation(libs.kotlinPoet)
    testImplementation(libs.kotlinCompileTesting)
    testImplementation(project(":common:truth-extensions"))
}
