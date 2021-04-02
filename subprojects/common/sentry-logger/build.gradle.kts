plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(project(":common:sentry"))
    api(project(":common:logger"))

    implementation(libs.kotlinStdlib)
}
