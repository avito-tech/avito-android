plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

dependencies {
    api(projects.subprojects.testRunner.testReportDsl)
    api(projects.subprojects.testRunner.fileStorage)
    api(libs.espressoCore)
    api(libs.appcompat)

    implementation(projects.subprojects.common.okhttp)
    implementation(projects.subprojects.common.httpClient)
    implementation(projects.subprojects.common.time)
    implementation(projects.subprojects.testRunner.testReportArtifacts) {
        because("ExternalStorageTransport need to know where to store artifacts")
    }
    implementation(projects.subprojects.logger.logger)
    implementation(projects.subprojects.logger.elasticLogger)
    implementation(projects.subprojects.common.sentry)
    implementation(projects.subprojects.common.waiter)
    implementation(projects.subprojects.common.result)
    implementation(projects.subprojects.testRunner.testAnnotations)
    implementation(projects.subprojects.common.throwableUtils)
    implementation(projects.subprojects.androidTest.instrumentation)
    implementation(projects.subprojects.androidTest.resourceManagerExceptions)
    implementation(projects.subprojects.androidTest.websocketReporter)
    implementation(libs.androidXTestCore)
    implementation(libs.okio)
    implementation(libs.kotson)
    implementation(libs.okhttp)
    implementation(libs.radiography)

    testImplementation(libs.jsonPathAssert)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.okhttpMock)
    testImplementation(projects.subprojects.common.junitUtils)
    testImplementation(projects.subprojects.common.resources)
    testImplementation(projects.subprojects.common.truthExtensions)
    testImplementation(testFixtures(projects.subprojects.logger.logger))
    testImplementation(testFixtures(projects.subprojects.common.time))
    testImplementation(testFixtures(projects.subprojects.common.httpClient))
}
