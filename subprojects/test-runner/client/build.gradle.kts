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
    api(projects.testRunner.service)
    api(projects.common.result)

    implementation(projects.testRunner.testAnnotations)
    implementation(projects.testRunner.instrumentationTestsDexLoader)
    implementation(projects.testRunner.testReportArtifacts)
    implementation(projects.testRunner.deviceProvider.impl)
    implementation(projects.testRunner.reportProcessor)
    implementation(projects.gradle.traceEvent)
    implementation(projects.common.throwableUtils)
    implementation(projects.common.math)
    implementation(projects.common.compositeException)
    implementation(projects.common.result)
    implementation(projects.common.problem)
    implementation(projects.common.files)
    implementation(projects.testRunner.fileStorage)
    implementation(libs.coroutinesCore)
    implementation(libs.gson)
    implementation(libs.commonsText) {
        because("for StringEscapeUtils.escapeXml10() only")
    }

    testImplementation(libs.coroutinesTest)
    testImplementation(libs.kotlinReflect)
    testImplementation(libs.mockitoJUnitJupiter)
    testImplementation(libs.mockitoKotlin)
    testImplementation(projects.common.truthExtensions)
    testImplementation(projects.gradle.testProject)
    testImplementation(testFixtures(projects.common.time))
    testImplementation(testFixtures(projects.logger.logger))
    testImplementation(testFixtures(projects.testRunner.instrumentationTestsDexLoader))
    testImplementation(testFixtures(projects.testRunner.report))
    testImplementation(testFixtures(projects.testRunner.reportViewer))
    testImplementation(testFixtures(projects.testRunner.service))

    testFixturesImplementation(testFixtures(projects.common.time))
    testFixturesImplementation(testFixtures(projects.logger.logger))
    testFixturesImplementation(testFixtures(projects.testRunner.deviceProvider.impl))
    testFixturesImplementation(testFixtures(projects.testRunner.report))
    testFixturesImplementation(testFixtures(projects.testRunner.service))
}
