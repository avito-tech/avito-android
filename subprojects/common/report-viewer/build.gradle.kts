plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
    id("convention.test-fixtures")
}

dependencies {
    api(libs.funktionaleTry)
    api(libs.gson)

    api(project(":common:okhttp"))

    implementation(project(":common:math"))
    implementation(project(":common:logger"))
    implementation(libs.okhttpLogging)
    implementation(libs.kotson)

    testImplementation(project(":common:truth-extensions"))
    testImplementation(project(":common:test-okhttp"))
    testImplementation(project(":common:files"))
    testImplementation(project(":common:resources"))
    testImplementation(testFixtures(project(":common:logger")))
    testImplementation(testFixtures(project(":common:report-viewer")))
    testImplementation(libs.okhttpMockWebServer)
    testImplementation(libs.jsonPathAssert)

    testFixturesImplementation(project(":common:test-okhttp"))
    testFixturesImplementation(libs.okhttpMockWebServer)
    testFixturesImplementation(testFixtures(project(":common:logger")))
    testFixturesImplementation(libs.junitJupiterApi)
}
