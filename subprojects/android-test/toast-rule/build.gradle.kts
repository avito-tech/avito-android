plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

dependencies {
    implementation(libs.androidAnnotations)
    implementation(libs.junit)

    implementation(projects.subprojects.androidLib.proxyToast)
    implementation(projects.subprojects.androidTest.uiTestingCore)
    implementation(projects.subprojects.common.junitUtils)
}
