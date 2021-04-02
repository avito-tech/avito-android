plugins {
    id("convention.kotlin-jvm")
}

dependencies {
    api(project(":gradle:process"))

    implementation(libs.kotlinStdlib)
}
