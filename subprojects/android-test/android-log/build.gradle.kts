plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
    id("convention.libraries")
}

dependencies {
    api(project(":common:logger"))

    implementation(project(":common:elastic-logger"))
    implementation(project(":common:sentry-logger"))
}
