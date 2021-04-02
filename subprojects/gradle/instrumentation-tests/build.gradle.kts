import com.avito.android.test.applyOptionalSystemProperty

plugins {
    id("convention.kotlin-jvm")
    id("convention.test-fixtures")
    id("convention.publish-gradle-plugin")
    id("convention.integration-testing")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    api(projects.gradle.kubernetes)
    api(projects.common.time)
    api(projects.gradle.buildVerdictTasksApi)

    implementation(libs.commonsIo) {
        because("LogcatBuffer.Impl.tailer needs to consider Charset (https://issues.apache.org/jira/browse/IO-354)")
    }
    implementation(libs.commonsText)
    implementation(libs.coroutinesCore)
    implementation(libs.gson)
    implementation(libs.kotson)
    implementation(libs.retrofit)
    implementation(libs.teamcityClient)
    implementation(projects.common.compositeException)
    implementation(projects.common.fileStorage)
    implementation(projects.common.logger)
    implementation(projects.common.reportViewer)
    implementation(projects.common.retrace)
    implementation(projects.common.httpClient)
    implementation(projects.common.throwableUtils)
    implementation(projects.common.result)
    implementation(projects.common.sentry)
    implementation(projects.common.testAnnotations)
    implementation(projects.gradle.android)
    implementation(projects.gradle.gradleLogger)
    implementation(projects.gradle.statsdConfig)
    implementation(projects.common.files)
    implementation(projects.gradle.buildEnvironment)
    implementation(projects.gradle.git)
    implementation(projects.gradle.instrumentationChangedTestsFinder)
    implementation(projects.gradle.instrumentationTestsDexLoader)
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.gradle.process)
    implementation(projects.gradle.runner.report)
    implementation(projects.gradle.runner.client)
    implementation(projects.gradle.runner.deviceProvider)
    implementation(projects.gradle.runner.stub)
    implementation(projects.gradle.teamcity)
    implementation(projects.gradle.uploadCdBuildResult)
    implementation(projects.gradle.buildFailer)
    implementation(projects.gradle.worker)

    testImplementation(projects.common.truthExtensions)
    testImplementation(projects.gradle.buildFailerTestFixtures)
    testImplementation(projects.gradle.instrumentationTestsDexLoaderTestFixtures)
    testImplementation(testFixtures(projects.common.httpClient))

    gradleTestImplementation(projects.gradle.testProject)

    integTestImplementation(projects.common.statsd)

    testFixturesApi(testFixtures(projects.common.logger))
    testFixturesApi(testFixtures(projects.common.time))
    testFixturesApi(testFixtures(projects.common.reportViewer))
    testFixturesApi(testFixtures(projects.gradle.runner.deviceProvider))
    testFixturesApi(testFixtures(projects.gradle.runner.report))
    testFixturesApi(testFixtures(projects.gradle.runner.client))
}

kotlin {
    explicitApi()
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

tasks.named<Test>("integrationTest").configure {
    applyOptionalSystemProperty("kubernetesUrl")
    applyOptionalSystemProperty("kubernetesToken")
    applyOptionalSystemProperty("kubernetesCaCertData")
    applyOptionalSystemProperty("kubernetesNamespace")
}
