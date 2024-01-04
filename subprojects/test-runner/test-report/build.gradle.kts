plugins {
    id("convention.kotlin-jvm-android")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(project(":subprojects:test-runner:test-report-dsl"))
    api(project(":subprojects:test-runner:file-storage"))

    implementation(project(":subprojects:common:reflection-extensions"))
    implementation(project(":subprojects:common:time"))
    implementation(project(":subprojects:test-runner:test-report-artifacts")) {
        because("ExternalStorageTransport need to know where to store artifacts")
    }
    implementation(project(":subprojects:logger:logger"))
    implementation(project(":subprojects:logger:elastic-logger"))
    implementation(project(":subprojects:common:sentry"))
    implementation(project(":subprojects:common:result"))
    implementation(project(":subprojects:test-runner:test-annotations"))
    implementation(project(":subprojects:common:throwable-utils"))
    implementation(project(":subprojects:android-test:resource-manager-exceptions"))
    implementation(project(":subprojects:android-test:websocket-reporter"))
    implementation(libs.okio)
    implementation(libs.kotson)
    implementation(libs.okhttp)

    testImplementation(libs.jsonPathAssert)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.okhttpMock)
    testImplementation(project(":subprojects:common:junit-utils"))
    testImplementation(project(":subprojects:common:truth-extensions"))
    testImplementation(testFixtures(project(":subprojects:common:time")))
}
