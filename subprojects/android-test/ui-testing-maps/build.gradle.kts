plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

dependencies {
    api(project(":android-test:ui-testing-core"))
    api(libs.playServicesMaps)
}
