plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

android {
    namespace = "com.avito.android.test.inhouse"
}

dependencies {
    api(project(":subprojects:test-runner:test-instrumentation-runner"))
    api(project(":subprojects:common:junit-utils"))
    api(project(":subprojects:test-runner:test-report-dsl-api"))
    api(project(":subprojects:test-runner:test-report-android")) // TODO: use as implementation
    api(project(":subprojects:common:statsd"))
    api(project(":subprojects:common:elastic"))
    api(project(":subprojects:common:test-okhttp"))
    api(libs.okhttpMockWebServer)

    implementation(project(":subprojects:common:build-metadata"))
    implementation(project(":subprojects:logger:android-logger"))
    implementation(project(":subprojects:logger:elastic-logger"))
    implementation(project(":subprojects:common:http-statsd"))
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:common:resources"))
    implementation(project(":subprojects:test-runner:report-viewer")) {
        because("knows about avito report model: ReportCoordinates, RunId for LocalRunTrasport from test-report")
    }
    implementation(project(":subprojects:test-runner:test-report-artifacts")) {
        because("uses factory to create TestArtifactsProvider")
    }
    implementation(project(":subprojects:test-runner:shared:logger-providers"))
    implementation(project(":subprojects:test-runner:transport"))
    implementation(project(":subprojects:logger:logger"))
    implementation(project(":subprojects:common:junit-utils"))
    implementation(project(":subprojects:test-runner:test-annotations"))
    implementation(project(":subprojects:test-runner:file-storage"))
    implementation(project(":subprojects:common:time"))
    implementation(project(":subprojects:android-test:ui-testing-core"))
    implementation(project(":subprojects:android-test:instrumentation"))

    implementation(libs.playServicesBase)
    implementation(libs.androidXTestRunner)
    implementation(libs.truth)
    implementation(libs.mockitoKotlin)
    implementation(libs.okhttpLogging)
    implementation(libs.gson)

    testImplementation(libs.kotlinPoet)
    testImplementation(libs.kotlinCompileTesting)
    testImplementation(project(":subprojects:common:truth-extensions"))
}
