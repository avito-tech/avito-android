plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(libs.gson)

    api(projects.common.okhttp)
    api(projects.common.result)
    api(projects.testRunner.reportApi)

    implementation(projects.common.math)
    implementation(projects.logger.logger)
    implementation(projects.common.httpClient)
    implementation(libs.kotson)

    testImplementation(projects.common.truthExtensions)
    testImplementation(projects.common.files)
    testImplementation(projects.common.resources)
    testImplementation(projects.common.testOkhttp)
    testImplementation(testFixtures(projects.logger.logger))
    testImplementation(testFixtures(projects.testRunner.reportViewer))
    testImplementation(testFixtures(projects.testRunner.reportApi))
    testImplementation(libs.okhttpMockWebServer)
    testImplementation(libs.jsonPathAssert)

    testFixturesImplementation(testFixtures(projects.logger.logger))
    testFixturesImplementation(testFixtures(projects.common.httpClient))
}
