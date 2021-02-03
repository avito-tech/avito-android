plugins {
    id("com.android.library")
    id("kotlin-android")
}

dependencies {
    api(Dependencies.appcompat)
    api(Dependencies.androidAnnotations)
    implementation(Dependencies.Test.mockitoCore)
    implementation(project(":subprojects:android-test:test-inhouse-runner"))
    implementation(project(":subprojects:android-test:ui-testing-core"))
    implementation(project(":subprojects:common:logger"))
    implementation(project(":subprojects:common:file-storage"))
    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:common:composite-exception"))
}
