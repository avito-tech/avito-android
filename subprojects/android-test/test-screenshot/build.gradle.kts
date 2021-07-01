plugins {
    id("convention.kotlin-android-library")
}

dependencies {
    api(libs.appcompat)
    api(libs.androidAnnotations)
    implementation(libs.mockitoKotlin)
    implementation(project(":test-runner:test-inhouse-runner"))
    implementation(project(":android-test:ui-testing-core"))
    implementation(project(":logger:logger"))
    implementation(project(":test-runner:file-storage"))
    implementation(project(":test-runner:report-viewer"))
    implementation(project(":common:composite-exception"))
}
