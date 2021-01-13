plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":common:logger"))

    implementation(project(":common:elastic-logger"))
    implementation(project(":common:sentry-logger"))
}
