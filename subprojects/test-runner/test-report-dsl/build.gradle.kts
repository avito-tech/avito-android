plugins {
    id("convention.kotlin-jvm-android")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(project(":subprojects:test-runner:test-report-api"))
    api(project(":subprojects:test-runner:test-report-dsl-api"))
    api(project(":subprojects:logger:logger"))
    api(libs.junit)
}
