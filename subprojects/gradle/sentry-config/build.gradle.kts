plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.subprojects.common.sentry)

    implementation(gradleApi())
    implementation(projects.subprojects.common.buildMetadata)
    implementation(projects.subprojects.common.okhttp)
    implementation(projects.subprojects.logger.logger)
    implementation(projects.subprojects.gradle.buildEnvironment)
    implementation(projects.subprojects.gradle.gradleExtensions)
}
