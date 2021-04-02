plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(gradleApi())
    api(project(":gradle:gradle-extensions"))
    implementation(project(":common:logger"))
    implementation(project(":gradle:git"))
    implementation(libs.kotlinStdlib)
}
