plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
    id("convention.libraries")
}

dependencies {
    api(project(":test-runner:test-report-dsl"))
    api(project(":common:file-storage"))
    api(libs.espressoCore)
    api(libs.appcompat)

    implementation(project(":common:okhttp"))
    implementation(project(":common:http-client"))
    implementation(project(":common:time"))
    implementation(project(":test-runner:test-report-artifacts")) {
        because("ExternalStorageTransport need to know where to store artifacts")
    }
    implementation(project(":logger:logger"))
    implementation(project(":logger:elastic-logger"))
    implementation(project(":common:sentry"))
    implementation(project(":common:waiter"))
    implementation(project(":common:result"))
    implementation(project(":test-runner:test-annotations"))
    implementation(project(":common:throwable-utils"))
    implementation(project(":logger:android-log"))
    implementation(project(":android-test:instrumentation"))
    implementation(project(":android-test:resource-manager-exceptions"))
    implementation(project(":android-test:websocket-reporter"))
    implementation(libs.androidXTestCore)
    implementation(libs.okio)
    implementation(libs.kotson)
    implementation(libs.okhttp)
    implementation(libs.radiography)

    testImplementation(libs.jsonPathAssert)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.okhttpMock)
    testImplementation(project(":common:junit-utils"))
    testImplementation(project(":common:resources"))
    testImplementation(project(":common:truth-extensions"))
    testImplementation(testFixtures(project(":logger:logger")))
    testImplementation(testFixtures(project(":common:time")))
    testImplementation(testFixtures(project(":common:http-client")))
}
