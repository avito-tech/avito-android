plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(project(":common:graphite"))

    implementation(gradleApi())
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":gradle:gradle-extensions"))

    implementation(libs.kotlinStdlib)
}
