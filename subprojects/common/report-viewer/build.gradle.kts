plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.funktionaleTry)
    api(Dependencies.gson)

    api(project(":subprojects:common:okhttp"))

    implementation(project(":subprojects:common:logger"))
    implementation(Dependencies.okhttpLogging)
    implementation(Dependencies.kotson)

    testImplementation(project(":subprojects:gradle:test-project")) //todo remove, we need to extract fileFromJarResources() to other module
    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(project(":subprojects:common:report-viewer-test-fixtures"))
    testImplementation(Dependencies.test.okhttpMockWebServer)
    testImplementation(Dependencies.test.jsonPathAssert)
}
