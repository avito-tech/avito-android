plugins {
    id("com.android.library")
    id("kotlin-android")
    id("digital.wup.android-maven-publish")
    `maven-publish`
}

dependencies {
    api(project(":subprojects:android-test:ui-testing-core"))
    api("com.google.android.gms:play-services-maps:17.0.0")
}
