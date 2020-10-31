plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(Dependencies.androidAnnotations)
    implementation(Dependencies.Test.junit)

    implementation(project(":android-lib:proxy-toast"))
    implementation(project(":android-test:ui-testing-core"))
    implementation(project(":common:junit-utils"))
}
