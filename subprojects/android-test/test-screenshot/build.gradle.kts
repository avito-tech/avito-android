plugins {
    id("convention.kotlin-android-library")
}

dependencies {
    api(libs.appcompat)
    api(libs.androidAnnotations)
    implementation(libs.mockitoKotlin)
    implementation(projects.testRunner.testInhouseRunner)
    implementation(projects.androidTest.uiTestingCore)
    implementation(projects.logger.logger)
    implementation(projects.testRunner.fileStorage)
    implementation(projects.testRunner.reportViewer)
    implementation(projects.common.compositeException)
}
