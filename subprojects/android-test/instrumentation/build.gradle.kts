plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

dependencies {
    api(projects.common.result)

    implementation(libs.androidXTestCore)
}
