plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.subprojects.testRunner.testReportDsl)
    api(projects.subprojects.testRunner.fileStorage)

    implementation(projects.subprojects.common.okhttp)
    implementation(projects.subprojects.common.reflectionExtensions)
    implementation(projects.subprojects.common.httpClient)
    implementation(projects.subprojects.common.time)
    implementation(projects.subprojects.testRunner.testReportArtifacts) {
        because("ExternalStorageTransport need to know where to store artifacts")
    }
    implementation(projects.subprojects.logger.logger)
    implementation(projects.subprojects.logger.elasticLogger)
    implementation(projects.subprojects.common.sentry)
    implementation(projects.subprojects.common.result)
    implementation(projects.subprojects.testRunner.testAnnotations)
    implementation(projects.subprojects.common.throwableUtils)
    implementation(projects.subprojects.androidTest.resourceManagerExceptions)
    implementation(projects.subprojects.androidTest.websocketReporter)
    implementation(libs.androidXTestCore)
    implementation(libs.okio)
    implementation(libs.kotson)
    implementation(libs.okhttp)

    testImplementation(libs.jsonPathAssert)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.okhttpMock)
    testImplementation(projects.subprojects.common.junitUtils)
    testImplementation(projects.subprojects.common.truthExtensions)
    testImplementation(testFixtures(projects.subprojects.common.time))
    testImplementation(testFixtures(projects.subprojects.common.httpClient))
}
