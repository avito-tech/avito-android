plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
    id("convention.test-fixtures")
}

dependencies {
    api(libs.funktionaleTry)
    api(libs.gson)

    api(project(":subprojects:common:okhttp"))

    implementation(project(":subprojects:common:math"))
    implementation(project(":subprojects:common:logger"))
    implementation(libs.okhttpLogging)
    implementation(libs.kotson)

    testImplementation(project(":subprojects:common:truth-extensions"))
    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(project(":subprojects:common:files"))
    testImplementation(project(":subprojects:common:resources"))
    testImplementation(testFixtures(project(":subprojects:common:logger")))
    testImplementation(testFixtures(project(":subprojects:common:report-viewer")))
    testImplementation(libs.okhttpMockWebServer)
    testImplementation(libs.jsonPathAssert)

    testFixturesImplementation(project(":subprojects:common:test-okhttp"))
    testFixturesImplementation(libs.okhttpMockWebServer)
    testFixturesImplementation(testFixtures(project(":subprojects:common:logger")))
    testFixturesImplementation(libs.junitJupiterApi)
}
