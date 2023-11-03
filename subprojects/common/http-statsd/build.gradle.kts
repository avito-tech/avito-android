plugins {
    id("convention.kotlin-jvm-android")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.subprojects.common.time)
    api(projects.subprojects.common.statsd)
    api(projects.subprojects.logger.logger)
    api(projects.subprojects.common.okhttp)

    testImplementation(projects.subprojects.common.testOkhttp)
    testImplementation(projects.subprojects.common.truthExtensions)
    testImplementation(projects.subprojects.common.junitUtils)
    testImplementation(testFixtures(projects.subprojects.common.statsd))
}
