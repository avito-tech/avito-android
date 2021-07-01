plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

dependencies {
    api(libs.androidXTestRunner)
    implementation(libs.uiAutomator)
    implementation(projects.logger.logger)
}
