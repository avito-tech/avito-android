plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
    id("com.avito.android.libraries")
}

dependencies {
    implementation(libs.androidAnnotations)
    implementation(libs.junit)

    implementation(project(":subprojects:android-lib:proxy-toast"))
    implementation(project(":subprojects:android-test:ui-testing-core"))
    implementation(project(":subprojects:common:junit-utils"))
}
