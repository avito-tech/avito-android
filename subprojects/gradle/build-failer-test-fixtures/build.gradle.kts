plugins {
    id("convention.kotlin-jvm")
}

dependencies {
    api(project(":gradle:build-failer"))

    implementation(libs.kotlinStdlib)
}
