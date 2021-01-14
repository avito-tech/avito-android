plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.AndroidTest.runner)
    implementation(Dependencies.AndroidTest.uiAutomator)
    implementation(project(":common:logger"))
}
