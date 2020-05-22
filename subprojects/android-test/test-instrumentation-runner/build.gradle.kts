plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.androidTest.runner)
    implementation(Dependencies.androidTest.uiAutomator)
}
