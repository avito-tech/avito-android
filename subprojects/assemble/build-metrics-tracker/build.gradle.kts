plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(projects.subprojects.common.statsd)
    implementation(projects.subprojects.gradle.buildEnvironment)
    implementation(projects.subprojects.gradle.statsdConfig)

    testImplementation(testFixtures(projects.subprojects.common.graphite))
    testImplementation(testFixtures(projects.subprojects.common.statsd))
    testImplementation(testFixtures(projects.subprojects.gradle.buildEnvironment))
}
