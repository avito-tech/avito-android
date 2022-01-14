plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(projects.subprojects.testRunner.testModel)
    api(projects.subprojects.testRunner.runnerApi)
    api(projects.subprojects.testRunner.report)
    api(projects.subprojects.common.httpClient)
    api(projects.subprojects.logger.logger)

    implementation(projects.subprojects.testRunner.testReportArtifacts)
    implementation(projects.subprojects.testRunner.fileStorage)
    implementation(projects.subprojects.common.time)
    implementation(projects.subprojects.common.problem)
    implementation(projects.subprojects.common.retrace)
    implementation(projects.subprojects.common.throwableUtils)
    implementation(libs.coroutinesCore)
    implementation(libs.commonsIo) {
        because("LogcatBuffer.Impl.tailer needs to consider Charset (https://issues.apache.org/jira/browse/IO-354)")
    }

    testImplementation(projects.subprojects.common.truthExtensions)
    testImplementation(testFixtures(projects.subprojects.common.time))
    testImplementation(testFixtures(projects.subprojects.testRunner.report))
    testImplementation(testFixtures(projects.subprojects.testRunner.runnerApi))
}
