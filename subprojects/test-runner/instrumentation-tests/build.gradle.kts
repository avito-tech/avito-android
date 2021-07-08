plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    api(projects.gradle.buildVerdictTasksApi)
    api(projects.gradle.kubernetes)

    implementation(libs.gson)
    implementation(projects.common.time)
    implementation(projects.gradle.android)
    implementation(projects.gradle.buildEnvironment)
    implementation(projects.gradle.buildFailer)
    implementation(projects.gradle.git)
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.gradle.statsdConfig)
    implementation(projects.gradle.worker)
    implementation(projects.logger.gradleLogger)
    implementation(projects.logger.logger)
    implementation(projects.testRunner.client)
    implementation(projects.testRunner.deviceProvider.model)
    implementation(projects.testRunner.instrumentationChangedTestsFinder)
    implementation(projects.testRunner.report)
    implementation(projects.testRunner.reportViewer)
    implementation(projects.testRunner.testAnnotations)

    testImplementation(projects.common.truthExtensions)
    testImplementation(testFixtures(projects.common.httpClient))
    testImplementation(testFixtures(projects.testRunner.report))
    testImplementation(testFixtures(projects.testRunner.reportViewer))
    testImplementation(testFixtures(projects.testRunner.client))
    testImplementation(testFixtures(projects.testRunner.instrumentationTestsDexLoader))

    gradleTestImplementation(projects.gradle.testProject)
    gradleTestImplementation(projects.common.truthExtensions)
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
