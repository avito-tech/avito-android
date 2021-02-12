plugins {
    id("com.avito.android.kotlin-android-library")
    id("com.avito.android.publish-android-library")
    id("com.avito.android.libraries")
}

dependencies {
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:common:time"))
    implementation(project(":subprojects:common:file-storage"))
    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:common:logger"))
    implementation(project(":subprojects:common:elastic-logger"))
    implementation(project(":subprojects:common:sentry"))
    implementation(project(":subprojects:common:test-annotations"))
    implementation(project(":subprojects:common:throwable-utils"))
    implementation(project(":subprojects:android-test:android-log"))
    implementation(project(":subprojects:android-test:ui-testing-core"))
    implementation(project(":subprojects:android-test:resource-manager-exceptions"))
    implementation(project(":subprojects:android-test:websocket-reporter"))
    implementation(libs.okio)
    implementation(libs.kotson)
    implementation(libs.funktionaleTry)
    implementation(libs.okhttp)
    implementation(libs.sentryAndroid) // todo use common:sentry

    testImplementation(project(":subprojects:common:resources"))
    testImplementation(project(":subprojects:common:junit-utils"))
    testImplementation(testFixtures(project(":subprojects:common:logger")))
    testImplementation(testFixtures(project(":subprojects:common:time")))
    testImplementation(libs.okhttpMock)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.jsonPathAssert)
    testImplementation(project(":subprojects:common:truth-extensions"))
}
