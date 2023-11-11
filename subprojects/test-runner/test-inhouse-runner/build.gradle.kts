plugins {
    id("convention.kotlin-android-library")
    id("convention.publish-android-library")
}

dependencies {
    api(projects.subprojects.testRunner.testInstrumentationRunner)
    api(projects.subprojects.common.junitUtils)
    api(projects.subprojects.testRunner.testReportDslApi)
    api(projects.subprojects.testRunner.testReportAndroid) // TODO: use as implementation
    api(projects.subprojects.common.statsd)
    api(projects.subprojects.common.elastic)
    api(projects.subprojects.common.testOkhttp)
    api(libs.okhttpMockWebServer)

    implementation(projects.subprojects.common.buildMetadata)
    implementation(projects.subprojects.logger.androidLogger)
    implementation(projects.subprojects.logger.elasticLogger)
    implementation(projects.subprojects.common.httpStatsd)
    implementation(projects.subprojects.common.okhttp)
    implementation(projects.subprojects.common.resources)
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
    implementation(projects.subprojects.testRunner.testAnnotations)
    implementation(projects.subprojects.testRunner.fileStorage)
    implementation(projects.subprojects.common.time)
    implementation(projects.subprojects.androidTest.uiTestingCore)
    implementation(projects.subprojects.androidTest.instrumentation)

    implementation(libs.playServicesBase)
    implementation(libs.androidXTestRunner)
    implementation(libs.truth)
    implementation(libs.mockitoKotlin)
    implementation(libs.okhttpLogging)
    implementation(libs.gson)

    testImplementation(libs.kotlinPoet)
    testImplementation(libs.kotlinCompileTesting)
    testImplementation(projects.subprojects.common.truthExtensions)
}
