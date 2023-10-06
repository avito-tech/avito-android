plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    implementation(projects.subprojects.testRunner.testAnnotations)
    implementation(projects.subprojects.testRunner.reportViewerModel)
    implementation(projects.subprojects.testRunner.reportViewerTestStaticDataParser)
    implementation(projects.subprojects.testRunner.instrumentationTestsDexLoader)
    implementation(projects.subprojects.testRunner.testModel)
    implementation(projects.subprojects.logger.logger)
    implementation(libs.gson)
    implementation(libs.androidAnnotations)

    testImplementation(projects.subprojects.common.truthExtensions)
    testImplementation(testFixtures(projects.subprojects.testRunner.instrumentationTestsDexLoader))
}
