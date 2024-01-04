plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

dependencies {
    api(project(":subprojects:common:result"))

    implementation(libs.androidXTestCore)
}
