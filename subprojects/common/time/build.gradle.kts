plugins {
    id("convention.kotlin-jvm-android")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(project(":subprojects:logger:logger"))
    api(libs.androidAnnotations)
}
