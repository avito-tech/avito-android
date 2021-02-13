plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
    id("com.avito.android.libraries")
}

dependencies {
    api(libs.androidXTestRunner)
    implementation(libs.uiAutomator)
    implementation(project(":subprojects:common:logger"))
}
