plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

android {
    namespace = "com.avito.android.logger"
}

dependencies {
    api(project(":subprojects:logger:logger"))
}
