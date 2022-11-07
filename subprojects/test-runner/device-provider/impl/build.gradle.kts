plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(projects.subprojects.testRunner.service)
    api(projects.subprojects.testRunner.deviceProvider.api)
    api(projects.subprojects.testRunner.kubernetes)

    implementation(projects.subprojects.gradle.process)
    implementation(projects.subprojects.logger.logger)
    implementation(projects.subprojects.common.result)
    implementation(projects.subprojects.common.waiter)

    testImplementation(libs.coroutinesTest)
    testImplementation(testFixtures(projects.subprojects.common.time))

    testFixturesApi(testFixtures(projects.subprojects.testRunner.kubernetes))
}
