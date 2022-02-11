plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

dependencies {
    api(projects.subprojects.testRunner.testReport)
    api(libs.espressoCore)
    api(libs.appcompat)

    implementation(projects.subprojects.testRunner.testReportArtifacts) {
        because("ExternalStorageTransport need to know where to store artifacts")
    }
    implementation(projects.subprojects.common.waiter)
    implementation(projects.subprojects.androidTest.instrumentation)
    implementation(libs.radiography)

    testImplementation(projects.subprojects.common.resources)
}
