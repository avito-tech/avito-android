plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    api(projects.subprojects.assemble.buildVerdictTasksApi)
    api(projects.subprojects.testRunner.kubernetes)

    implementation(libs.gson)
    implementation(projects.subprojects.common.time)
    implementation(projects.subprojects.gradle.android)
    implementation(projects.subprojects.gradle.buildEnvironment)
    implementation(projects.subprojects.gradle.buildFailer)
    implementation(projects.subprojects.gradle.git)
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.gradle.statsdConfig)
    implementation(projects.subprojects.gradle.worker)
    implementation(projects.subprojects.logger.gradleLogger)
    implementation(projects.subprojects.logger.logger)
    implementation(projects.subprojects.testRunner.client)
    implementation(projects.subprojects.testRunner.deviceProvider.model)
    implementation(projects.subprojects.testRunner.instrumentationChangedTestsFinder)
    implementation(projects.subprojects.testRunner.report)
    implementation(projects.subprojects.testRunner.reportViewer)
    implementation(projects.subprojects.testRunner.testAnnotations)

    testImplementation(projects.subprojects.common.truthExtensions)
    testImplementation(testFixtures(projects.subprojects.common.httpClient))
    testImplementation(testFixtures(projects.subprojects.testRunner.report))
    testImplementation(testFixtures(projects.subprojects.testRunner.reportViewer))
    testImplementation(testFixtures(projects.subprojects.testRunner.client))
    testImplementation(testFixtures(projects.subprojects.testRunner.instrumentationTestsDexLoader))

    gradleTestImplementation(projects.subprojects.gradle.testProject)
    gradleTestImplementation(projects.subprojects.common.truthExtensions)
}

gradlePlugin {
    plugins {
        create("functionalTests") {
            id = "com.avito.android.instrumentation-tests"
            implementationClass = "com.avito.instrumentation.InstrumentationTestsPlugin"
            displayName = "Instrumentation tests"
        }
    }
}
