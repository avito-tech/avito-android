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
    implementation(project(":android-test:ui-testing-core"))
    implementation(project(":android-test:resource-manager-exceptions"))
    implementation(project(":android-test:websocket-reporter"))
    implementation(Dependencies.okio)
    implementation(Dependencies.kotson)
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.okhttp)
    implementation(Dependencies.sentryAndroid) //todo use common:sentry

    testImplementation(project(":android-test:junit-utils"))
    testImplementation(Dependencies.test.okhttpMock)
    testImplementation(Dependencies.test.mockitoKotlin)
    testImplementation(Dependencies.test.truth)
    testImplementation(Dependencies.test.jsonPathAssert)
}
