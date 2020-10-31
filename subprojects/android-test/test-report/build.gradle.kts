plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":common:okhttp"))
    implementation(project(":common:time"))
    implementation(project(":common:file-storage"))
    implementation(project(":common:report-viewer"))
    implementation(project(":common:logger"))
    implementation(project(":common:test-annotations"))
    implementation(project(":common:throwable-utils"))
    implementation(project(":android-test:ui-testing-core"))
    implementation(project(":android-test:resource-manager-exceptions"))
    implementation(project(":android-test:websocket-reporter"))
    implementation(Dependencies.okio)
    implementation(Dependencies.kotson)
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.okhttp)
    implementation(Dependencies.sentryAndroid) //todo use common:sentry

    testImplementation(project(":common:junit-utils"))
    testImplementation(project(":common:logger-test-fixtures"))
    testImplementation(project(":common:time-test-fixtures"))
    testImplementation(Dependencies.Test.okhttpMock)
    testImplementation(Dependencies.Test.mockitoKotlin)
    testImplementation(Dependencies.Test.jsonPathAssert)
    testImplementation(project(":common:truth-extensions"))
}
