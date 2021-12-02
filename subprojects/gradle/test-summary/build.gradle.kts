plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.test-fixtures")
}

dependencies {
    implementation(libs.okhttp)
    implementation(projects.subprojects.common.httpClient)
    implementation(projects.subprojects.common.math)
    implementation(projects.subprojects.common.problem)
    implementation(projects.subprojects.common.time)
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.gradle.slack)
    implementation(projects.subprojects.gradle.statsdConfig)
    implementation(projects.subprojects.logger.slf4jGradleLogger)
    implementation(projects.subprojects.testRunner.reportViewer) {
        because("API to fetch reports; ReportCoordinates model")
    }

    testImplementation(projects.subprojects.common.truthExtensions)
    testImplementation(testFixtures(projects.subprojects.testRunner.reportViewer))

    gradleTestImplementation(projects.subprojects.gradle.testProject)

    testFixturesImplementation(testFixtures(projects.subprojects.testRunner.testModel))
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
