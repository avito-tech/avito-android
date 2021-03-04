plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    implementation(project(":common:time"))
    implementation(project(":common:logger"))

    implementation(libs.retrofit)
    implementation(libs.okhttp)
}
