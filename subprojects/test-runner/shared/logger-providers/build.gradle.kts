plugins {
    id("convention.kotlin-jvm-android")
    id("convention.publish-kotlin-library")
}

dependencies {
    implementation(project(":subprojects:logger:logger"))
}
