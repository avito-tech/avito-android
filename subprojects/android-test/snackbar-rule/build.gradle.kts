plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":android-test:junit-utils"))
    api(project(":android-lib:snackbar-proxy"))
    implementation(project(":android-test:ui-testing-core"))
}
