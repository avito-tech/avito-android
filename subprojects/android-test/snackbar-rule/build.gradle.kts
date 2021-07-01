plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

dependencies {
    api(projects.common.junitUtils)
    api(projects.androidLib.snackbarProxy)
    implementation(projects.androidTest.uiTestingCore)
}
