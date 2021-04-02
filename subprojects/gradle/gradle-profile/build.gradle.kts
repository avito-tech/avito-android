plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    implementation(gradleApi())
    implementation(project(":gradle:gradle-extensions"))

    implementation(libs.kotlinStdlib)
}
