plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

dependencies {
    api(projects.testRunner.testInstrumentationRunner)
    api(projects.common.junitUtils)
    api(projects.testRunner.testReportDslApi)
    api(libs.sentry) {
        because("InHouseInstrumentationTestRunner.sentry")
    }

    implementation(projects.common.buildMetadata)
    implementation(projects.common.sentry)
    implementation(projects.logger.elasticLogger)
    implementation(projects.common.httpClient)
    implementation(projects.common.okhttp)
    implementation(projects.common.statsd)
    implementation(projects.testRunner.reportViewer) {
        because("knows about avito report model: ReportCoordinates, RunId for LocalRunTrasport from test-report")
    }
    implementation(projects.testRunner.testReportArtifacts) {
        because("uses factory to create TestArtifactsProvider")
    }
    implementation(projects.logger.logger)
    implementation(projects.common.junitUtils)
    implementation(projects.common.testOkhttp)
    implementation(projects.testRunner.testAnnotations)
    implementation(projects.testRunner.fileStorage)
    implementation(projects.common.time)
    implementation(projects.logger.androidLog)
    implementation(projects.androidTest.uiTestingCore)
    implementation(libs.playServicesBase)
    implementation(projects.androidTest.instrumentation)
    implementation(projects.testRunner.testReport)
    implementation(libs.androidXTestRunner)
    implementation(libs.truth)
    implementation(libs.mockitoKotlin)
    implementation(libs.okhttpLogging)
    implementation(libs.okhttpMockWebServer)
    implementation(libs.gson)

    testImplementation(libs.kotlinPoet)
    testImplementation(libs.kotlinCompileTesting)
    testImplementation(projects.common.truthExtensions)
}
