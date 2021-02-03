plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.funktionaleTry)
    api(Dependencies.gson)

    api(project(":subprojects:common:okhttp"))

    implementation(project(":subprojects:common:math"))
    implementation(project(":subprojects:common:logger"))
    implementation(Dependencies.okhttpLogging)
    implementation(Dependencies.kotson)

    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(project(":subprojects:common:files"))
    testImplementation(project(":subprojects:common:resources"))
    testImplementation(project(":subprojects:common:logger-test-fixtures"))
    testImplementation(project(":subprojects:common:report-viewer-test-fixtures"))
    testImplementation(Dependencies.Test.okhttpMockWebServer)
    testImplementation(Dependencies.Test.jsonPathAssert)
}
