plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":subprojects:common:junit-utils"))
    api(project(":subprojects:android-lib:snackbar-proxy"))
    implementation(project(":subprojects:android-test:ui-testing-core"))
}
