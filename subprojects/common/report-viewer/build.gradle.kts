plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
    `java-test-fixtures`
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
    testImplementation(testFixtures(project(":subprojects:common:logger")))
    testImplementation(testFixtures(project(":subprojects:common:report-viewer")))
    testImplementation(Dependencies.Test.okhttpMockWebServer)
    testImplementation(Dependencies.Test.jsonPathAssert)

    testFixturesImplementation(testFixtures(project(":subprojects:common:logger")))
    testFixturesImplementation(project(":subprojects:common:test-okhttp"))
}
