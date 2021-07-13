plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(projects.testRunner.service)
    api(projects.testRunner.deviceProvider.api)
    api(projects.gradle.kubernetes)

    implementation(projects.gradle.process)
    implementation(projects.logger.logger)
    implementation(projects.common.result)
    implementation(projects.common.waiter)

    testImplementation(testFixtures(projects.logger.logger))
    testImplementation(libs.coroutinesTest)

    testFixturesApi(testFixtures(projects.common.httpClient))
    testFixturesApi(testFixtures(projects.gradle.kubernetes))
}
