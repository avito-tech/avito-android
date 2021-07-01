plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    implementation(projects.common.okhttp)
    implementation(projects.common.statsd)
    implementation(projects.common.time)
    implementation(projects.logger.logger)

    testImplementation(projects.common.testOkhttp)
    testImplementation(projects.common.truthExtensions)
    testImplementation(testFixtures(projects.common.statsd))
    testImplementation(testFixtures(projects.logger.logger))

    testFixturesApi(testFixtures(projects.common.statsd))
    testFixturesApi(testFixtures(projects.logger.logger))
    testFixturesApi(testFixtures(projects.common.time))
}

kotlin {
    explicitApi()
}
