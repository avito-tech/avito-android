plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":android-test:test-instrumentation-runner"))
    api(project(":common:junit-utils"))
    api(project(":android-test:test-report"))
    api(Dependencies.sentry) {
        because("InHouseInstrumentationTestRunner.sentry")
    }

    implementation(project(":common:sentry"))
    implementation(project(":common:elastic-logger"))
    implementation(project(":common:okhttp"))
    implementation(project(":common:statsd"))
    implementation(project(":common:report-viewer"))
    implementation(project(":common:logger"))
    implementation(project(":common:junit-utils"))
    implementation(project(":common:test-okhttp"))
    implementation(project(":common:test-annotations"))
    implementation(project(":common:file-storage"))
    implementation(project(":common:time"))
    implementation(project(":android-test:android-log"))
    implementation(project(":android-test:ui-testing-core"))
    implementation(project(":android-test:ui-testing-maps"))
    implementation(Dependencies.AndroidTest.runner)
    implementation(Dependencies.Test.truth)
    implementation(Dependencies.Test.mockitoKotlin)
    implementation(Dependencies.okhttpLogging)
    implementation(Dependencies.Test.okhttpMockWebServer)
    implementation(Dependencies.gson)

    testImplementation(Dependencies.Test.kotlinPoet)
    testImplementation(Dependencies.Test.kotlinCompileTesting)
}
