plugins {
    id("convention.kotlin-android-library")
    id("convention.libraries")
}

dependencies {
    api(libs.appcompat)
    api(libs.androidAnnotations)
    implementation(libs.mockitoKotlin)
    implementation(project(":android-test:test-inhouse-runner"))
    implementation(project(":android-test:ui-testing-core"))
    implementation(project(":common:logger"))
    implementation(project(":common:file-storage"))
    implementation(project(":common:report-viewer"))
    implementation(project(":common:composite-exception"))
}
