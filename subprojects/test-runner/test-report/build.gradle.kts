plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

dependencies {
    api(projects.testRunner.testReportDsl)
    api(projects.testRunner.fileStorage)
    api(libs.espressoCore)
    api(libs.appcompat)

    implementation(projects.common.okhttp)
    implementation(projects.common.httpClient)
    implementation(projects.common.time)
    implementation(projects.testRunner.testReportArtifacts) {
        because("ExternalStorageTransport need to know where to store artifacts")
    }
    implementation(projects.logger.logger)
    implementation(projects.logger.elasticLogger)
    implementation(projects.common.sentry)
    implementation(projects.common.waiter)
    implementation(projects.common.result)
    implementation(projects.testRunner.testAnnotations)
    implementation(projects.common.throwableUtils)
    implementation(projects.logger.androidLog)
    implementation(projects.androidTest.instrumentation)
    implementation(projects.androidTest.resourceManagerExceptions)
    implementation(projects.androidTest.websocketReporter)
    implementation(libs.androidXTestCore)
    implementation(libs.okio)
    implementation(libs.kotson)
    implementation(libs.okhttp)
    implementation(libs.radiography)

    testImplementation(libs.jsonPathAssert)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.okhttpMock)
    testImplementation(projects.common.junitUtils)
    testImplementation(projects.common.resources)
    testImplementation(projects.common.truthExtensions)
    testImplementation(testFixtures(projects.logger.logger))
    testImplementation(testFixtures(projects.common.time))
    testImplementation(testFixtures(projects.common.httpClient))
}
