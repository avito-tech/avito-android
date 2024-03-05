plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

android {
    namespace = "com.avito.android.snackbar.rule"
}

dependencies {
    api(project(":subprojects:common:junit-utils"))
    api(project(":subprojects:android-lib:snackbar-proxy"))
    implementation(project(":subprojects:android-test:ui-testing-core"))
}
