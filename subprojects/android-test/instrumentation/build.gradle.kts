plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

dependencies {
    api(projects.subprojects.common.result)

    implementation(libs.androidXTestCore)
}
