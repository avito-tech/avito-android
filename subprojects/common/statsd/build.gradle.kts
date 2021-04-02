plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    implementation(libs.kotlinStdlib)
    implementation(libs.statsd)
    implementation(project(":common:logger"))
}
