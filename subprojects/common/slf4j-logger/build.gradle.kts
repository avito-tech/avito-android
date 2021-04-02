plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(project(":common:logger"))

    implementation(libs.slf4jApi)
    implementation(libs.kotlinStdlib)
}
