plugins {
    id("com.android.library")
    id("kotlin-android")
    id("digital.wup.android-maven-publish")
    `maven-publish`
    id("com.jfrog.bintray")
}

val junitVersion: String by project

dependencies {
    implementation("androidx.annotation:annotation:1.1.0")
    implementation("junit:junit:$junitVersion")

    implementation(project(":subprojects:android-lib:proxy-toast"))
    implementation(project(":subprojects:android-test:ui-testing-core"))
    implementation(project(":subprojects:android-test:junit-utils"))
}
