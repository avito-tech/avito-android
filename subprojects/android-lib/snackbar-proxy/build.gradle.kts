plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

android {
    namespace = "com.avito.android.snackbar.proxy"
}

dependencies {
    api(libs.material)
    implementation(libs.androidAnnotations)
}
