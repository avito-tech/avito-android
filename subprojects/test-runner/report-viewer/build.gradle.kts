plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
    id("convention.test-fixtures")
}

dependencies {
    api(libs.gson)

    api(project(":common:okhttp"))
    api(project(":common:result"))
    api(project(":common:report-api"))

    implementation(project(":common:math"))
    implementation(project(":logger:logger"))
    implementation(project(":common:http-client"))
    implementation(libs.kotson)

    testImplementation(project(":common:truth-extensions"))
    testImplementation(project(":common:files"))
    testImplementation(project(":common:resources"))
    testImplementation(project(":common:test-okhttp"))
    testImplementation(testFixtures(project(":logger:logger")))
    testImplementation(testFixtures(project(":test-runner:report-viewer")))
    testImplementation(testFixtures(project(":common:report-api")))
    testImplementation(libs.okhttpMockWebServer)
    testImplementation(libs.jsonPathAssert)

    testFixturesImplementation(testFixtures(project(":logger:logger")))
    testFixturesImplementation(testFixtures(project(":common:http-client")))
}

kotlin {
    explicitApi()
}
