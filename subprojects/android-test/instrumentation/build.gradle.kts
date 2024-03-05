plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

android {
    namespace = "com.avito.android.instrumentation"
}

dependencies {
    api(project(":subprojects:common:result"))

    implementation(libs.androidXTestCore)
}
