plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.subprojects.common.statsd)
    implementation(projects.subprojects.gradle.buildEnvironment)

    testImplementation(testFixtures(projects.subprojects.common.graphite))
    testImplementation(testFixtures(projects.subprojects.common.statsd))
    testImplementation(testFixtures(projects.subprojects.gradle.buildEnvironment))
}
