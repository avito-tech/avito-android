plugins {
    id("com.avito.android.kotlin-android-library")
    id("com.avito.android.publish-android-library")
    id("com.avito.android.libraries")
}

dependencies {
    api(project(":subprojects:android-test:ui-testing-core"))
    api(libs.playServicesMaps)
}
