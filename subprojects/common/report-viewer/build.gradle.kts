plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:common:logger"))
    implementation(Dependencies.okhttpLogging)
    implementation(Dependencies.gson)
    implementation(Dependencies.kotson)
    implementation(Dependencies.funktionaleTry)

    testImplementation(project(":subprojects:gradle:test-project")) //todo remove, we need to extract fileFromJarResources() to other module
    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(Dependencies.test.okhttpMockWebServer)
    testImplementation(Dependencies.test.jsonPathAssert)

    testFixturesImplementation(project(":subprojects:common:test-okhttp"))
    testFixturesImplementation(project(":subprojects:common:logger"))
    testFixturesImplementation(Dependencies.gson)
    testFixturesImplementation(Dependencies.funktionaleTry)
}
