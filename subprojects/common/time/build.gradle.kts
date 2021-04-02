plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(project(":common:logger"))

    implementation(libs.kotlinStdlib)
}
