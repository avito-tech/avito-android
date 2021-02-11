plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":subprojects:android-test:ui-testing-core"))
    api(Dependencies.playServicesMaps)
}
