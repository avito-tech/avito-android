plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

dependencies {
    api(projects.subprojects.common.junitUtils)
    api(projects.subprojects.androidLib.snackbarProxy)
    implementation(projects.subprojects.androidTest.uiTestingCore)
}
