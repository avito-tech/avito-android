plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.funktionaleTry)
    api(Dependencies.gson)

    api(project(":common:okhttp"))
    api(project(":common:percent"))

    implementation(project(":common:logger"))
    implementation(Dependencies.okhttpLogging)
    implementation(Dependencies.kotson)

    testImplementation(project(":common:test-okhttp"))
    testImplementation(project(":common:files"))
    testImplementation(project(":common:resources"))
    testImplementation(project(":common:logger-test-fixtures"))
    testImplementation(project(":common:report-viewer-test-fixtures"))
    testImplementation(Dependencies.Test.okhttpMockWebServer)
    testImplementation(Dependencies.Test.jsonPathAssert)
}
