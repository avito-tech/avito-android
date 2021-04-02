plugins {
    id("convention.kotlin-jvm")
}

dependencies {
    api(project(":gradle:git"))

    implementation(libs.kotlinStdlib)
}
