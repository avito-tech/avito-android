plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-kotlin-library")
    id("com.avito.android.libraries")
    `java-test-fixtures`
}

dependencies {
    api(libs.funktionaleTry)
    api(libs.gson)

    api(project(":subprojects:common:okhttp"))

    implementation(project(":subprojects:common:math"))
    implementation(project(":subprojects:common:logger"))
    implementation(libs.okhttpLogging)
    implementation(libs.kotson)

    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(project(":subprojects:common:files"))
    testImplementation(project(":subprojects:common:resources"))
    testImplementation(testFixtures(project(":subprojects:common:logger")))
    testImplementation(testFixtures(project(":subprojects:common:report-viewer")))
    testImplementation(libs.okhttpMockWebServer)
    testImplementation(libs.jsonPathAssert)

    testFixturesImplementation(testFixtures(project(":subprojects:common:logger")))
    testFixturesImplementation(project(":subprojects:common:test-okhttp"))
    testFixturesImplementation(libs.junitJupiterApi)
}
