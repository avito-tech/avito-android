plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.common.sentry)

    implementation(gradleApi())
    implementation(projects.common.buildMetadata)
    implementation(projects.common.okhttp)
    implementation(projects.logger.logger)
    implementation(projects.gradle.buildEnvironment)
    implementation(projects.gradle.gradleExtensions)
}
