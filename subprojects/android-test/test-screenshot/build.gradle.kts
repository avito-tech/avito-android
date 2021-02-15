plugins {
    id("convention.kotlin-android-library")
    id("convention.libraries")
}

dependencies {
    api(libs.appcompat)
    api(libs.androidAnnotations)
    implementation(libs.mockitoKotlin)
    implementation(project(":subprojects:android-test:test-inhouse-runner"))
    implementation(project(":subprojects:android-test:ui-testing-core"))
    implementation(project(":subprojects:common:logger"))
    implementation(project(":subprojects:common:file-storage"))
    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:common:composite-exception"))
}
