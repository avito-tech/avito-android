plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
    id("convention.libraries")
}

dependencies {
    implementation(project(":common:okhttp"))
    implementation(project(":common:time"))
    implementation(project(":common:file-storage"))
    implementation(project(":common:report-viewer"))
    implementation(project(":common:logger"))
    implementation(project(":common:elastic-logger"))
    implementation(project(":common:sentry"))
    implementation(project(":common:test-annotations"))
    implementation(project(":common:throwable-utils"))
    implementation(project(":android-test:android-log"))
    implementation(project(":android-test:ui-testing-core"))
    implementation(project(":android-test:resource-manager-exceptions"))
    implementation(project(":android-test:websocket-reporter"))
    implementation(libs.okio)
    implementation(libs.kotson)
    implementation(libs.funktionaleTry)
    implementation(libs.okhttp)
    implementation(libs.sentryAndroid) // todo use common:sentry
    implementation(libs.radiography)

    testImplementation(project(":common:resources"))
    testImplementation(project(":common:junit-utils"))
    testImplementation(testFixtures(project(":common:logger")))
    testImplementation(testFixtures(project(":common:time")))
    testImplementation(libs.okhttpMock)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.jsonPathAssert)
    testImplementation(project(":common:truth-extensions"))
}
