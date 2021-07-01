plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

dependencies {
    api(libs.material)
    implementation(libs.androidAnnotations)
}
