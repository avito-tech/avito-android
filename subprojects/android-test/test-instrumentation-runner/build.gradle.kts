plugins {
    id("com.avito.android.kotlin-android-library")
    id("com.avito.android.publish-android-library")
    id("com.avito.android.libraries")
}

dependencies {
    api(libs.androidXTestRunner)
    implementation(libs.uiAutomator)
    implementation(project(":subprojects:common:logger"))
}
