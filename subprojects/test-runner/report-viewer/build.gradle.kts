plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(libs.gson)

    api(projects.common.okhttp)
    api(projects.common.result)
    api(projects.testRunner.report)
    api(libs.androidAnnotations)

    implementation(libs.kotson)
    implementation(projects.common.httpClient)
    implementation(projects.logger.logger)
    implementation(projects.testRunner.testModel)

    testImplementation(libs.jsonPathAssert)
    testImplementation(libs.okhttpMockWebServer)
    testImplementation(projects.common.files)
    testImplementation(projects.common.resources)
    testImplementation(projects.common.testOkhttp)
    testImplementation(projects.common.truthExtensions)
    testImplementation(testFixtures(projects.logger.logger))
    testImplementation(testFixtures(projects.testRunner.report))
    testImplementation(testFixtures(projects.testRunner.reportViewer))

    testFixturesImplementation(testFixtures(projects.common.httpClient))
    testFixturesImplementation(testFixtures(projects.logger.logger))
}
