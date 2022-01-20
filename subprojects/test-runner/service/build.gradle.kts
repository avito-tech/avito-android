plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

publish {
    artifactId.set("runner-service")
}

dependencies {
    api(projects.subprojects.common.coroutinesExtension)
    api(projects.subprojects.common.statsd)
    api(projects.subprojects.common.time)
    api(projects.subprojects.gradle.process)
    api(projects.subprojects.testRunner.commandLineExecutor)
    api(projects.subprojects.testRunner.deviceProvider.api)
    api(projects.subprojects.testRunner.testModel)
    api(projects.subprojects.testRunner.runnerApi)

    implementation(projects.subprojects.common.result)
    implementation(projects.subprojects.common.problem)
    implementation(projects.subprojects.testRunner.testReportArtifacts) {
        because("DeviceWorker pulls test artifacts")
    }
    implementation(libs.ddmlib)
    implementation(libs.rxJava)
    implementation(libs.kotlinStdlibJdk7) {
        because("java.nio.file.Path extensions")
    }

    testImplementation(libs.coroutinesTest)
    testImplementation(projects.subprojects.common.files)
    testImplementation(projects.subprojects.common.resources)
    testImplementation(projects.subprojects.common.truthExtensions)
    testImplementation(projects.subprojects.logger.logger)
    testImplementation(testFixtures(projects.subprojects.common.time))
    testImplementation(testFixtures(projects.subprojects.testRunner.deviceProvider.model))
}
