plugins {
    id("convention.kotlin-android-library")
    id("convention.libraries")
}

dependencies {
    api(libs.appcompat)
    api(libs.androidAnnotations)
    implementation(libs.mockitoKotlin)
    implementation(projects.androidTest.testInhouseRunner)
    implementation(projects.androidTest.uiTestingCore)
    implementation(projects.common.logger)
    implementation(projects.common.fileStorage)
    implementation(projects.common.reportViewer)
    implementation(projects.common.compositeException)
}
