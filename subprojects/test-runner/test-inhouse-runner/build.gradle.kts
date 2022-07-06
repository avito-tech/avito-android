plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

dependencies {
    api(projects.subprojects.testRunner.testInstrumentationRunner)
    api(projects.subprojects.common.junitUtils)
    api(projects.subprojects.testRunner.testReportDslApi)
    api(projects.subprojects.testRunner.testReportAndroid) // TODO: use as implementation

    implementation(projects.subprojects.common.buildMetadata)
    implementation(projects.subprojects.common.sentry)
    implementation(projects.subprojects.logger.androidLogger)
    implementation(projects.subprojects.logger.elasticLogger)
    implementation(projects.subprojects.logger.sentryLogger)
    implementation(projects.subprojects.common.httpClient)
    implementation(projects.subprojects.common.okhttp)
    implementation(projects.subprojects.common.statsd)
    implementation(projects.subprojects.testRunner.reportViewer) {
        because("knows about avito report model: ReportCoordinates, RunId for LocalRunTrasport from test-report")
    }
    implementation(projects.subprojects.testRunner.testReportArtifacts) {
        because("uses factory to create TestArtifactsProvider")
    }
    implementation(projects.subprojects.testRunner.shared.loggerProviders)
    implementation(projects.subprojects.testRunner.transport)
    implementation(projects.subprojects.logger.logger)
    implementation(projects.subprojects.common.junitUtils)
    implementation(projects.subprojects.common.testOkhttp)
    implementation(projects.subprojects.testRunner.testAnnotations)
    implementation(projects.subprojects.testRunner.fileStorage)
    implementation(projects.subprojects.common.time)
    implementation(projects.subprojects.androidTest.uiTestingCore)
    implementation(libs.playServicesBase)
    implementation(projects.subprojects.androidTest.instrumentation)
    implementation(libs.androidXTestRunner)
    implementation(libs.truth)
    implementation(libs.mockitoKotlin)
    implementation(libs.okhttpLogging)
    implementation(libs.okhttpMockWebServer)
    implementation(libs.gson)

    testImplementation(libs.kotlinPoet)
    testImplementation(libs.kotlinCompileTesting)
    testImplementation(projects.subprojects.common.truthExtensions)
}
