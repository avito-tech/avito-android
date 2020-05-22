plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(Dependencies.androidAnnotations)
    implementation(Dependencies.test.junit)

    implementation(project(":subprojects:android-lib:proxy-toast"))
    implementation(project(":subprojects:android-test:ui-testing-core"))
    implementation(project(":subprojects:android-test:junit-utils"))
}
