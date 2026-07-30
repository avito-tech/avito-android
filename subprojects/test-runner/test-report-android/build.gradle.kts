plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

android {
    namespace = "com.avito.android.test.report"
}

dependencies {
    api(project(":subprojects:test-runner:test-report"))
    api(libs.espressoCore)
    api(libs.appcompat)

    implementation(project(":subprojects:test-runner:test-report-artifacts")) {
        because("ExternalStorageTransport need to know where to store artifacts")
    }
    implementation(project(":subprojects:common:reflection-extensions"))
    api(project(":subprojects:common:statsd")) {
        because("StatsDSender is a part of VideoCaptureTestListener public api")
    }
    api(project(":subprojects:common:time")) {
        because("TimeProvider is a part of VideoCaptureTestListener public api")
    }
    implementation(project(":subprojects:common:waiter"))
    implementation(project(":subprojects:android-test:instrumentation"))
    implementation(libs.radiography)

    testImplementation(project(":subprojects:common:resources"))
    testImplementation(testFixtures(project(":subprojects:common:statsd")))
}
