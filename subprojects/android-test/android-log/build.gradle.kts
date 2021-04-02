plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

dependencies {
    api(project(":common:logger"))

    implementation(libs.kotlinStdlib)
    implementation(project(":common:elastic-logger"))
    implementation(project(":common:sentry-logger"))
}
