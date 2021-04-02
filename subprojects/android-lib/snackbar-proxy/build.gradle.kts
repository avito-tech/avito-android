plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

dependencies {
    api(libs.material)

    implementation(libs.kotlinStdlib)
    implementation(libs.androidAnnotations)
}
