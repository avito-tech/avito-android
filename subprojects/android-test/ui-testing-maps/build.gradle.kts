plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
    id("com.avito.android.libraries")
}

dependencies {
    api(project(":subprojects:android-test:ui-testing-core"))
    api(libs.playServicesMaps)
}
