plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    implementation(projects.subprojects.common.math)
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.logger.gradleLogger)
    implementation(gradleApi())
    implementation(libs.googlePublish)
}
