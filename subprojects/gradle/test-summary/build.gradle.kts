plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.testRunner.reportViewer)
    implementation(projects.common.time)
    implementation(projects.common.math)
    implementation(projects.common.problem)
    implementation(projects.common.httpClient)
    implementation(projects.logger.gradleLogger)
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.gradle.slack)
    implementation(projects.gradle.statsdConfig)
    implementation(libs.okhttp)

    testImplementation(projects.common.truthExtensions)
    testImplementation(testFixtures(projects.testRunner.reportViewer))

    gradleTestImplementation(projects.gradle.testProject)
}

gradlePlugin {
    plugins {
        create("testSummary") {
            id = "com.avito.android.test-summary"
            implementationClass = "com.avito.test.summary.TestSummaryPlugin"
            displayName = "Instrumentation tests summary"
        }
    }
}
