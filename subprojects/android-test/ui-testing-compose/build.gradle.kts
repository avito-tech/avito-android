plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

android {
    namespace = "com.avito.android.test.compose"
}

dependencies {
    api(libs.compose.uiTestJunit)
    api(project(":subprojects:android-test:ui-testing-core"))
}
