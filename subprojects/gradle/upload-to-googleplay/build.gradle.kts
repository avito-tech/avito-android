plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    implementation(projects.common.math)
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.gradle.gradleLogger)
    implementation(gradleApi())
    implementation(libs.googlePublish)
}
