plugins {
    id("convention.kotlin-android-library")
}

dependencies {
    api(libs.appcompat)
    api(libs.androidAnnotations)
    implementation(libs.mockitoKotlin)
    implementation(projects.subprojects.testRunner.testInhouseRunner)
    implementation(projects.subprojects.androidTest.uiTestingCore)
    implementation(projects.subprojects.logger.logger)
    implementation(projects.subprojects.testRunner.fileStorage)
    implementation(projects.subprojects.testRunner.reportViewer)
    implementation(projects.subprojects.common.compositeException)
}
