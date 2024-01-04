plugins {
    id("convention.kotlin-jvm-android")
    id("convention.publish-kotlin-library")
}

dependencies {
    implementation(project(":subprojects:test-runner:report-viewer-model"))
}
