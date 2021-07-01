plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.common.graphite)

    implementation(gradleApi())
    implementation(projects.logger.gradleLogger)
    implementation(projects.gradle.gradleExtensions)
}
