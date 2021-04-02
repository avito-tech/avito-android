plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(project(":common:logger"))
    api(project(":common:elastic"))

    implementation(libs.kotlinStdlib)
}
