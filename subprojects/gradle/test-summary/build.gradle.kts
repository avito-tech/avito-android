plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.test-fixtures")
}

dependencies {
    implementation(libs.okhttp)
    implementation(projects.common.httpClient)
    implementation(projects.common.math)
    implementation(projects.common.problem)
    implementation(projects.common.time)
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.gradle.slack)
    implementation(projects.gradle.statsdConfig)
    implementation(projects.logger.gradleLogger)
    implementation(projects.testRunner.reportViewer) {
        because("API to fetch reports; ReportCoordinates model")
    }

    testImplementation(projects.common.truthExtensions)
    testImplementation(testFixtures(projects.testRunner.reportViewer))

    gradleTestImplementation(projects.gradle.testProject)

    testFixturesImplementation(testFixtures(projects.testRunner.testModel))
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
