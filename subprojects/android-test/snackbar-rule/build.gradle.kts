plugins {
    id("com.avito.android.kotlin-android-library")
    id("com.avito.android.publish-android-library")
}

dependencies {
    api(project(":subprojects:common:junit-utils"))
    api(project(":subprojects:android-lib:snackbar-proxy"))
    implementation(project(":subprojects:android-test:ui-testing-core"))
}
