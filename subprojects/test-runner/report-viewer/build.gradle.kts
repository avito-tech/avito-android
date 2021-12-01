plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(libs.gson)

    api(projects.subprojects.common.okhttp)
    api(projects.subprojects.common.result)
    api(projects.subprojects.testRunner.report)
    api(libs.androidAnnotations)

    implementation(libs.kotson)
    implementation(projects.subprojects.common.httpClient)
    implementation(projects.subprojects.testRunner.testModel)

    testImplementation(libs.jsonPathAssert)
    testImplementation(libs.okhttpMockWebServer)
    testImplementation(projects.subprojects.common.files)
    testImplementation(projects.subprojects.common.resources)
    testImplementation(projects.subprojects.common.testOkhttp)
    testImplementation(projects.subprojects.common.truthExtensions)
    testImplementation(testFixtures(projects.subprojects.testRunner.report))
    testImplementation(testFixtures(projects.subprojects.testRunner.reportViewer))

    testFixturesImplementation(testFixtures(projects.subprojects.common.httpClient))
}
