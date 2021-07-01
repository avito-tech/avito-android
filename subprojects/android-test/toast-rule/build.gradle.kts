plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

dependencies {
    implementation(libs.androidAnnotations)
    implementation(libs.junit)

    implementation(projects.androidLib.proxyToast)
    implementation(projects.androidTest.uiTestingCore)
    implementation(projects.common.junitUtils)
}
