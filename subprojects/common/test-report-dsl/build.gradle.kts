plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    api(project(":common:test-report-api"))
    api(project(":common:test-report-dsl-api"))
    api(project(":common:logger"))
    api(libs.junit)
}
