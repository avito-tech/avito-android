plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.subprojects.common.statsd)

    implementation(gradleApi())
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.logger.slf4jGradleLogger)
}
