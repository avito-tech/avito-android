plugins {
    id("convention.kotlin-jvm-android")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(libs.gson)

    api(project(":subprojects:common:okhttp"))
    api(project(":subprojects:common:result"))
    api(project(":subprojects:test-runner:report"))
    api(libs.androidAnnotations)

    implementation(libs.kotson)
    implementation(project(":subprojects:common:http-statsd")) {
        because("RequestMetadata")
    }
    implementation(project(":subprojects:test-runner:test-model"))

    testImplementation(libs.jsonPathAssert)
    testImplementation(libs.okhttpMockWebServer)
    testImplementation(project(":subprojects:common:files"))
    testImplementation(project(":subprojects:common:resources"))
    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(project(":subprojects:common:truth-extensions"))
    testImplementation(testFixtures(project(":subprojects:test-runner:report")))
    testImplementation(testFixtures(project(":subprojects:test-runner:report-viewer")))
}
