plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
    id("convention.libraries")
}

dependencies {
    api(libs.androidXTestRunner)
    implementation(libs.uiAutomator)
    implementation(projects.common.logger)
}
