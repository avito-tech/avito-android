plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

android {
    namespace = "com.avito.android.proxytoast"
}

dependencies {
    implementation(libs.androidAnnotations)
}
