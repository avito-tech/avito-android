plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(projects.testRunner.testModel)
    api(projects.testRunner.runnerApi)
    api(projects.testRunner.report)
    api(projects.common.httpClient)
    api(projects.logger.logger)

    implementation(projects.testRunner.testReportArtifacts)
    implementation(projects.testRunner.fileStorage)
    implementation(projects.common.time)
    implementation(projects.common.problem)
    implementation(projects.common.retrace)
    implementation(projects.common.throwableUtils)
    implementation(libs.coroutinesCore)
    implementation(libs.commonsIo) {
        because("LogcatBuffer.Impl.tailer needs to consider Charset (https://issues.apache.org/jira/browse/IO-354)")
    }

    testImplementation(projects.common.truthExtensions)
    testImplementation(testFixtures(projects.common.time))
    testImplementation(testFixtures(projects.logger.logger))
    testImplementation(testFixtures(projects.testRunner.report))
    testImplementation(testFixtures(projects.testRunner.runnerApi))
}
