plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
    id("convention.libraries")
}

dependencies {
    api(projects.androidTest.testInstrumentationRunner)
    api(projects.common.junitUtils)
    api(projects.androidTest.testReport)
    api(libs.sentry) {
        because("InHouseInstrumentationTestRunner.sentry")
    }

    implementation(projects.common.buildMetadata)
    implementation(projects.common.sentry)
    implementation(projects.common.elasticLogger)
    implementation(projects.common.httpClient)
    implementation(projects.common.okhttp)
    implementation(projects.common.statsd)
    implementation(projects.common.reportViewer)
    implementation(projects.common.logger)
    implementation(projects.common.junitUtils)
    implementation(projects.common.testOkhttp)
    implementation(projects.common.testAnnotations)
    implementation(projects.common.fileStorage)
    implementation(projects.common.time)
    implementation(projects.androidTest.androidLog)
    implementation(projects.androidTest.uiTestingCore)
    implementation(projects.androidTest.uiTestingMaps)
    implementation(libs.androidXTestRunner)
    implementation(libs.truth)
    implementation(libs.mockitoKotlin)
    implementation(libs.okhttpLogging)
    implementation(libs.okhttpMockWebServer)
    implementation(libs.gson)

    testImplementation(libs.kotlinPoet)
    testImplementation(libs.kotlinCompileTesting)
    testImplementation(projects.common.truthExtensions)
}
