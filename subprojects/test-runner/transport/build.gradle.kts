plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    implementation(projects.subprojects.testRunner.report)
    implementation(projects.subprojects.testRunner.testReport)
    implementation(projects.subprojects.testRunner.testReportArtifacts)
    implementation(projects.subprojects.testRunner.reportViewer)
}
