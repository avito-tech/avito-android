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
    implementation(projects.gradle.uploadCdBuildResult)
    implementation(projects.gradle.worker)
    implementation(projects.logger.gradleLogger)
    implementation(projects.logger.logger)
    implementation(projects.testRunner.instrumentationChangedTestsFinder)
    implementation(projects.testRunner.instrumentationTestsDexLoader)
    implementation(projects.testRunner.report)
    implementation(projects.testRunner.testAnnotations)
    implementation(projects.testRunner.deviceProvider.model)
    implementation(projects.testRunner.client)

    testImplementation(projects.common.result)
    testImplementation(projects.testRunner.reportApi)
    testImplementation(projects.common.httpClient)
    testImplementation(projects.common.truthExtensions)
    testImplementation(projects.gradle.buildFailerTestFixtures)
    testImplementation(testFixtures(projects.common.httpClient))
    testImplementation(testFixtures(projects.testRunner.reportApi))
    testImplementation(testFixtures(projects.testRunner.reportViewer))
    testImplementation(testFixtures(projects.testRunner.client))
    testImplementation(testFixtures(projects.testRunner.deviceProvider.impl))
    testImplementation(testFixtures(projects.testRunner.instrumentationTestsDexLoader))
    testImplementation(testFixtures(projects.testRunner.report))

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
