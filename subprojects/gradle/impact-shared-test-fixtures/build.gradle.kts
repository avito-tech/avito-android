plugins {
    id("convention.kotlin-jvm")
}

dependencies {
    api(projects.common.result)

    implementation(gradleApi())
    implementation(projects.gradle.impactShared)
    implementation(projects.gradle.testProject)
    implementation(libs.truth)
}
