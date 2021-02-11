plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":subprojects:android-test:test-instrumentation-runner"))
    api(project(":subprojects:common:junit-utils"))
    api(project(":subprojects:android-test:test-report"))
    api(Dependencies.sentry) {
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
    implementation(Dependencies.AndroidTest.runner)
    implementation(Dependencies.Test.truth)
    implementation(Dependencies.Test.mockitoKotlin)
    implementation(Dependencies.okhttpLogging)
    implementation(Dependencies.Test.okhttpMockWebServer)
    implementation(Dependencies.gson)

    testImplementation(Dependencies.Test.kotlinPoet)
    testImplementation(Dependencies.Test.kotlinCompileTesting)
}
