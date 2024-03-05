plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

android {
    namespace = "com.avito.android.test.rx3idler"
}

dependencies {
    implementation(libs.rx3Ilder)
}
