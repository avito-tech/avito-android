plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(projects.subprojects.common.time)
    api(projects.subprojects.common.statsd)
    api(projects.subprojects.logger.logger)

    implementation(projects.subprojects.common.okhttp)

    testImplementation(projects.subprojects.common.testOkhttp)
    testImplementation(projects.subprojects.common.truthExtensions)
    testImplementation(projects.subprojects.common.junitUtils)
    testImplementation(testFixtures(projects.subprojects.common.statsd))

    testFixturesApi(testFixtures(projects.subprojects.common.statsd))
    testFixturesApi(testFixtures(projects.subprojects.common.time))
}
