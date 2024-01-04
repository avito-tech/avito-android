plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

dependencies {
    api(project(":subprojects:test-runner:test-report"))
    api(libs.espressoCore)
    api(libs.appcompat)

    implementation(project(":subprojects:test-runner:test-report-artifacts")) {
        because("ExternalStorageTransport need to know where to store artifacts")
    }
    implementation(project(":subprojects:common:reflection-extensions"))
    implementation(project(":subprojects:common:waiter"))
    implementation(project(":subprojects:android-test:instrumentation"))
    implementation(libs.radiography)

    testImplementation(project(":subprojects:common:resources"))
}
