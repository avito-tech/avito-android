plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(Dependencies.androidAnnotations)
    implementation(Dependencies.Test.junit)

    implementation(project(":subprojects:android-lib:proxy-toast"))
    implementation(project(":subprojects:android-test:ui-testing-core"))
    implementation(project(":subprojects:common:junit-utils"))
}
