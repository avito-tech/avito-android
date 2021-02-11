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
    implementation(project(":subprojects:common:elastic-logger"))
    implementation(project(":subprojects:common:sentry"))
    implementation(project(":subprojects:common:test-annotations"))
    implementation(project(":subprojects:common:throwable-utils"))
    implementation(project(":subprojects:android-test:android-log"))
    implementation(project(":subprojects:android-test:ui-testing-core"))
    implementation(project(":subprojects:android-test:resource-manager-exceptions"))
    implementation(project(":subprojects:android-test:websocket-reporter"))
    implementation(Dependencies.okio)
    implementation(Dependencies.kotson)
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.okhttp)
    implementation(Dependencies.sentryAndroid) // todo use common:sentry

    testImplementation(project(":subprojects:common:resources"))
    testImplementation(project(":subprojects:common:junit-utils"))
    testImplementation(testFixtures(project(":subprojects:common:logger")))
    testImplementation(testFixtures(project(":subprojects:common:time")))
    testImplementation(Dependencies.Test.okhttpMock)
    testImplementation(Dependencies.Test.mockitoKotlin)
    testImplementation(Dependencies.Test.jsonPathAssert)
    testImplementation(project(":subprojects:common:truth-extensions"))
}
