plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

dependencies {
    api(projects.androidTest.uiTestingCore)
    api(libs.playServicesMaps)
}
