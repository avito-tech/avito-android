plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    implementation(projects.subprojects.common.math)
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(gradleApi())
    implementation(libs.googlePublish)
    implementation(libs.okhttp)
}
