plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
    id("convention.test-fixtures")
}

dependencies {
    api(libs.gson)

    api(projects.common.okhttp)
    api(projects.common.result)

    implementation(projects.common.math)
    implementation(projects.common.logger)
    implementation(projects.common.httpClient)
    implementation(libs.kotson)

    testImplementation(projects.common.truthExtensions)
    testImplementation(projects.common.files)
    testImplementation(projects.common.resources)
    testImplementation(projects.common.testOkhttp)
    testImplementation(testFixtures(projects.common.logger))
    testImplementation(testFixtures(projects.common.reportViewer))
    testImplementation(libs.okhttpMockWebServer)
    testImplementation(libs.jsonPathAssert)

    testFixturesImplementation(testFixtures(projects.common.logger))
    testFixturesImplementation(testFixtures(projects.common.httpClient))
}
