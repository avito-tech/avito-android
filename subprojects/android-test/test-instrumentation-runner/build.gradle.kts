plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    id("com.jfrog.bintray")
    id("digital.wup.android-maven-publish")
}

dependencies {
    api(Dependencies.androidTest.runner)
    implementation(Dependencies.androidTest.uiAutomator)
}
