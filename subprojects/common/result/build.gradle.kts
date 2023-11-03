plugins {
    id("convention.kotlin-jvm-android")
    id("convention.publish-kotlin-library")
}

dependencies {
    implementation(projects.subprojects.common.compositeException)
}
