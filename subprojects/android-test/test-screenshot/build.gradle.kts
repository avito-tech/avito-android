plugins {
    id("com.android.library")
    id("kotlin-android")
}

dependencies {
    api(Dependencies.appcompat)
    api(Dependencies.androidAnnotations)
    implementation(Dependencies.Test.mockitoCore)
    implementation(project(":android-test:test-inhouse-runner"))
    implementation(project(":android-test:ui-testing-core"))
    implementation(project(":common:logger"))
    implementation(project(":common:file-storage"))
    implementation(project(":common:report-viewer"))
    implementation(project(":common:composite-exception"))
}
