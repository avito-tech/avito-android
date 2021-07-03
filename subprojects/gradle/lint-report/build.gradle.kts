import com.avito.android.test.applyOptionalSystemProperty

plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.integration-testing")
    id("convention.gradle-testing")
}

dependencies {
    api(projects.gradle.buildVerdictTasksApi)

    implementation(libs.okhttp)

    implementation(projects.common.httpClient)
    implementation(projects.common.okhttp)
    implementation(projects.common.sentry)
    implementation(projects.gradle.android)
    implementation(projects.gradle.bitbucket)
    implementation(projects.gradle.buildEnvironment)
    implementation(projects.logger.gradleLogger)
    implementation(projects.gradle.git)
    implementation(projects.gradle.impactShared)
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.gradle.slack)
    implementation(projects.gradle.statsdConfig)

    testImplementation(projects.common.truthExtensions)
    testImplementation(projects.gradle.slackTestFixtures)
    testImplementation(testFixtures(projects.logger.logger))

    integTestImplementation(projects.common.resources)
    integTestImplementation(testFixtures(projects.common.time))
    integTestImplementation(testFixtures(projects.common.statsd))

    gradleTestImplementation(projects.gradle.testProject)
}

gradlePlugin {
    plugins {
        create("lintReport") {
            id = "com.avito.android.lint-report"
            implementationClass = "com.avito.android.lint.LintReportPlugin"
            displayName = "Lint reports notifier"
        }
    }
}

tasks.named<Test>("integrationTest").configure {
    applyOptionalSystemProperty("avito.slack.test.channelId")
    applyOptionalSystemProperty("avito.slack.test.channel")
    applyOptionalSystemProperty("avito.slack.test.token")
    applyOptionalSystemProperty("avito.slack.test.workspace")
}
