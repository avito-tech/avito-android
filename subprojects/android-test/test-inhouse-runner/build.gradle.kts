plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    id("com.jfrog.bintray")
    id("digital.wup.android-maven-publish")
}

dependencies {
    implementation(project(":subprojects:common:sentry"))
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:common:statsd"))
    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:common:logger"))
    implementation(project(":subprojects:android-test:junit-utils"))
    api(project(":subprojects:android-test:test-report"))
    implementation(project(":subprojects:android-test:test-annotations"))
    implementation(project(":subprojects:android-test:ui-testing-core"))
    implementation(project(":subprojects:android-test:ui-testing-maps"))
    implementation(Dependencies.androidTest.runner)
    implementation(Dependencies.test.truth)
    implementation(Dependencies.test.mockitoKotlin)
    implementation(Dependencies.okhttpLogging)
    implementation(Dependencies.test.okhttpMockWebServer)
    implementation(Dependencies.gson)

    testImplementation(Dependencies.test.kotlinPoet)
    testImplementation(Dependencies.test.kotlinCompileTesting)
}
