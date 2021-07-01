plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(project(":test-runner:test-report-api"))
    api(project(":test-runner:test-report-dsl-api"))
    api(project(":logger:logger"))
    api(libs.junit)
}
