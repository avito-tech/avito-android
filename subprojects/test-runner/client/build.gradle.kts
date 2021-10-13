plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

publish {
    artifactId.set("runner-client")
}

dependencies {
    compileOnly(gradleApi())
    api(projects.subprojects.testRunner.service)
    api(projects.subprojects.testRunner.reportViewer) {
        because("ReportViewerConfig exposes ReportCoordinates; also RunId")
    }

    implementation(projects.subprojects.common.compositeException)
    implementation(projects.subprojects.common.coroutinesExtension)
    implementation(projects.subprojects.common.files)
    implementation(projects.subprojects.common.math)
    implementation(projects.subprojects.common.problem)
    implementation(projects.subprojects.common.result)
    implementation(projects.subprojects.gradle.process)
    implementation(projects.subprojects.gradle.traceEvent)
    implementation(projects.subprojects.testRunner.deviceProvider.api)
    implementation(projects.subprojects.testRunner.deviceProvider.impl)
    implementation(projects.subprojects.testRunner.instrumentationTestsDexLoader)
    implementation(projects.subprojects.testRunner.reportProcessor)
    implementation(projects.subprojects.testRunner.runnerApi)
    implementation(projects.subprojects.testRunner.testAnnotations)
    implementation(projects.subprojects.testRunner.testReportArtifacts)
    implementation(libs.coroutinesCore)
    implementation(libs.gson)
    implementation(libs.commonsText) {
        because("for StringEscapeUtils.escapeXml10() only")
    }

    testImplementation(libs.coroutinesTest)
    testImplementation(libs.kotlinReflect)
    testImplementation(libs.mockitoJUnitJupiter)
    testImplementation(projects.subprojects.common.truthExtensions)
    testImplementation(testFixtures(projects.subprojects.common.time))
    testImplementation(testFixtures(projects.subprojects.logger.logger))
    testImplementation(testFixtures(projects.subprojects.testRunner.instrumentationTestsDexLoader))
    testImplementation(testFixtures(projects.subprojects.testRunner.report))
    testImplementation(testFixtures(projects.subprojects.testRunner.reportViewer))
    testImplementation(testFixtures(projects.subprojects.testRunner.service))
    testImplementation(testFixtures(projects.subprojects.testRunner.deviceProvider.model))

    testFixturesImplementation(testFixtures(projects.subprojects.common.time))
    testFixturesImplementation(testFixtures(projects.subprojects.logger.logger))
    testFixturesImplementation(testFixtures(projects.subprojects.testRunner.deviceProvider.impl))
    testFixturesImplementation(testFixtures(projects.subprojects.testRunner.report))
    testFixturesImplementation(testFixtures(projects.subprojects.testRunner.service))
}
