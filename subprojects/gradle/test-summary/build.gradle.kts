plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:common:statsd"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:gradle:slack"))
    implementation(Dependencies.coroutinesCore)
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.okhttp)

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(testFixtures(project(":subprojects:common:report-viewer")))

    testFixturesImplementation(project(":subprojects:common:report-viewer"))
}
