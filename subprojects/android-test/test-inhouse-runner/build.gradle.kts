plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
    id("convention.libraries")
}

dependencies {
    api(project(":subprojects:android-test:test-instrumentation-runner"))
    api(project(":subprojects:common:junit-utils"))
    api(project(":subprojects:android-test:test-report"))
    api(libs.sentry) {
        because("InHouseInstrumentationTestRunner.sentry")
    }

    implementation(project(":subprojects:common:sentry"))
    implementation(project(":subprojects:common:elastic-logger"))
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:common:statsd"))
    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:common:logger"))
    implementation(project(":subprojects:common:junit-utils"))
    implementation(project(":subprojects:common:test-okhttp"))
    implementation(project(":subprojects:common:test-annotations"))
    implementation(project(":subprojects:common:file-storage"))
    implementation(project(":subprojects:common:time"))
    implementation(project(":subprojects:android-test:android-log"))
    implementation(project(":subprojects:android-test:ui-testing-core"))
    implementation(project(":subprojects:android-test:ui-testing-maps"))
    implementation(libs.androidXTestRunner)
    implementation(libs.truth)
    implementation(libs.mockitoKotlin)
    implementation(libs.okhttpLogging)
    implementation(libs.okhttpMockWebServer)
    implementation(libs.gson)

    testImplementation(libs.kotlinPoet)
    testImplementation(libs.kotlinCompileTesting)
}
