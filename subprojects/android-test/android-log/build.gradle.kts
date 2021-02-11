plugins {
    id("com.avito.android.kotlin-android-library")
    id("com.avito.android.publish-android-library")
    id("com.avito.android.libraries")
}

dependencies {
    api(project(":subprojects:common:logger"))

    implementation(project(":subprojects:common:elastic-logger"))
    implementation(project(":subprojects:common:sentry-logger"))
}
