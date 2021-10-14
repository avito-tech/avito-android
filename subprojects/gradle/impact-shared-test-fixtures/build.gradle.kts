plugins {
    id("convention.kotlin-jvm")
}

dependencies {
    api(projects.subprojects.common.result)

    implementation(gradleApi())
    implementation(projects.subprojects.gradle.impactShared)
    implementation(projects.subprojects.gradle.testProject)
    implementation(libs.truth)
}
