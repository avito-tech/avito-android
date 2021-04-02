plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(libs.gson)

    api(project(":common:okhttp"))
    api(project(":common:result"))

    implementation(project(":common:math"))
    implementation(project(":common:logger"))
    implementation(project(":common:http-client"))
    implementation(libs.kotlinStdlib)
    implementation(libs.kotson)

    testImplementation(project(":common:truth-extensions"))
    testImplementation(project(":common:files"))
    testImplementation(project(":common:resources"))
    testImplementation(project(":common:test-okhttp"))
    testImplementation(testFixtures(project(":common:logger")))
    testImplementation(testFixtures(project(":common:report-viewer")))
    testImplementation(libs.okhttpMockWebServer)
    testImplementation(libs.jsonPathAssert)

    testFixturesImplementation(testFixtures(project(":common:logger")))
    testFixturesImplementation(testFixtures(project(":common:http-client")))
}
