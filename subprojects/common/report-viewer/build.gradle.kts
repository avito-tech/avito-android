plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.funktionaleTry)
    api(Dependencies.gson)

    api(project(":common:okhttp"))

    implementation(project(":common:logger"))
    implementation(Dependencies.okhttpLogging)
    implementation(Dependencies.kotson)

    testImplementation(project(":gradle:test-project")) //todo remove, we need to extract fileFromJarResources() to other module
    testImplementation(project(":common:test-okhttp"))
    testImplementation(project(":common:report-viewer-test-fixtures"))
    testImplementation(Dependencies.test.okhttpMockWebServer)
    testImplementation(Dependencies.test.jsonPathAssert)
}
