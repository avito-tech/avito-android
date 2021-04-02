plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    api(projects.common.sentry)

    implementation(gradleApi())
    implementation(projects.common.buildMetadata)
    implementation(projects.common.okhttp)
    implementation(projects.common.logger)
    implementation(projects.gradle.buildEnvironment)
    implementation(projects.gradle.gradleExtensions)
}
