plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":android-test:test-instrumentation-runner"))
    api(project(":android-test:junit-utils"))
    api(project(":android-test:test-report"))
    implementation(project(":common:sentry"))
    implementation(project(":common:okhttp"))
    implementation(project(":common:statsd"))
    implementation(project(":common:report-viewer"))
    implementation(project(":common:logger"))
    implementation(project(":android-test:junit-utils"))
    api(project(":android-test:test-report"))
    implementation(project(":common:test-annotations"))
    implementation(project(":android-test:ui-testing-core"))
    implementation(project(":android-test:ui-testing-maps"))
    implementation(Dependencies.androidTest.runner)
    implementation(Dependencies.test.truth)
    implementation(Dependencies.test.mockitoKotlin)
    implementation(Dependencies.okhttpLogging)
    implementation(Dependencies.test.okhttpMockWebServer)
    implementation(Dependencies.gson)

    testImplementation(Dependencies.test.kotlinPoet)
    testImplementation(Dependencies.test.kotlinCompileTesting)
}
