plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
    id("convention.libraries")
}

dependencies {
    api(projects.androidTest.uiTestingCore)
    api(libs.playServicesMaps)
}
