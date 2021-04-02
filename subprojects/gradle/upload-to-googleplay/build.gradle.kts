plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    implementation(project(":common:math"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:gradle-logger"))
    implementation(gradleApi())
    implementation(libs.googlePublish)
    implementation(libs.kotlinStdlib)
}
