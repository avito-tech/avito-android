plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(libs.robolectric)
    api(libs.junit)

    implementation(projects.subprojects.logger.logger)
    implementation(projects.subprojects.testRunner.shared.loggerProviders)
    implementation(projects.subprojects.testRunner.testReportJvm)
    implementation(projects.subprojects.testRunner.testReportArtifacts)
    implementation(projects.subprojects.testRunner.reportViewer)
    implementation(projects.subprojects.testRunner.transport)
    implementation(projects.subprojects.common.sentry)
    implementation(projects.subprojects.common.elastic)
    implementation(projects.subprojects.common.buildMetadata)
}
