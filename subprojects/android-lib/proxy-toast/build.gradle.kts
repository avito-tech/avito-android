plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

dependencies {
    implementation(libs.kotlinStdlib)
    implementation(libs.androidAnnotations)
}
