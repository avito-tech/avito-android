plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
    id("convention.libraries")
}

dependencies {
    api(project(":logger:logger"))

    implementation(project(":logger:elastic-logger"))
    implementation(project(":logger:sentry-logger"))
}
