plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

dependencies {
    api(project(":common:junit-utils"))
    api(project(":android-lib:snackbar-proxy"))
    implementation(project(":android-test:ui-testing-core"))
}
