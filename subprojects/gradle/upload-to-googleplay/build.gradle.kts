plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    implementation(projects.common.math)
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.logger.gradleLogger)
    implementation(gradleApi())
    implementation(libs.googlePublish)
}
