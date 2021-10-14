plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    implementation(projects.subprojects.common.okhttp)
    implementation(projects.subprojects.common.statsd)
    implementation(projects.subprojects.common.time)
    implementation(projects.subprojects.logger.logger)

    testImplementation(projects.subprojects.common.testOkhttp)
    testImplementation(projects.subprojects.common.truthExtensions)
    testImplementation(projects.subprojects.common.junitUtils)
    testImplementation(testFixtures(projects.subprojects.common.statsd))
    testImplementation(testFixtures(projects.subprojects.logger.logger))

    testFixturesApi(testFixtures(projects.subprojects.common.statsd))
    testFixturesApi(testFixtures(projects.subprojects.logger.logger))
    testFixturesApi(testFixtures(projects.subprojects.common.time))
}
