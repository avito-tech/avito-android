plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.common.statsd)
    implementation(projects.gradle.buildEnvironment)

    testImplementation(testFixtures(projects.common.graphite))
    testImplementation(testFixtures(project(":common:statsd")))
    testImplementation(testFixtures(projects.gradle.buildEnvironment))
}
