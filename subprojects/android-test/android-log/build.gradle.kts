plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":subprojects:common:logger"))

    implementation(project(":subprojects:common:elastic-logger"))
    implementation(project(":subprojects:common:sentry-logger"))
}
