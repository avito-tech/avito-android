plugins {
    id("convention.kotlin-jvm")
}

dependencies {
    api(project(":gradle:slack"))

    implementation(libs.kotlinStdlib)
}
