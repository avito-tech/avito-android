plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
    id("convention.libraries")
}

dependencies {
    api(project(":subprojects:common:logger"))

    implementation(project(":subprojects:common:elastic-logger"))
    implementation(project(":subprojects:common:sentry-logger"))
}
