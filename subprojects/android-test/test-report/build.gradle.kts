plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:common:time"))
    implementation(project(":subprojects:common:file-storage"))
    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:common:logger"))
    implementation(project(":subprojects:android-test:test-annotations"))
    implementation(project(":subprojects:android-test:ui-testing-core"))
    implementation(project(":subprojects:android-test:resource-manager-exceptions"))
    implementation(project(":subprojects:android-test:websocket-reporter"))
    implementation(Dependencies.okio)
    implementation(Dependencies.kotson)
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.okhttp)
    implementation(Dependencies.sentryAndroid) //todo use common:sentry

    testImplementation(project(":subprojects:android-test:junit-utils"))
    testImplementation(Dependencies.test.okhttpMock)
    testImplementation(Dependencies.test.mockitoKotlin)
    testImplementation(Dependencies.test.truth)
    testImplementation(Dependencies.test.jsonPathAssert)
}
