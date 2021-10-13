plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.subprojects.common.statsd)

    implementation(gradleApi())
    implementation(projects.subprojects.logger.gradleLogger)
    implementation(projects.subprojects.gradle.gradleExtensions)
}
