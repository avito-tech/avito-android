plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

dependencies {
    api(libs.androidXTestRunner)

    implementation(libs.kotlinStdlib)
    implementation(libs.uiAutomator)
    implementation(project(":common:logger"))
}
