import com.avito.android.test.applyOptionalSystemProperty

plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.integration-testing")
    id("convention.gradle-testing")
}

dependencies {
    api(project(":gradle:build-verdict-tasks-api"))

    implementation(libs.okhttp)

    implementation(project(":common:http-client"))
    implementation(project(":common:okhttp"))
    implementation(project(":common:sentry"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:bitbucket"))
    implementation(project(":gradle:build-environment"))
    implementation(project(":logger:gradle-logger"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:impact-shared"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:slack"))
    implementation(project(":gradle:statsd-config"))

    testImplementation(project(":common:truth-extensions"))
    testImplementation(project(":gradle:slack-test-fixtures"))
    testImplementation(testFixtures(project(":logger:logger")))

    integTestImplementation(project(":common:resources"))
    integTestImplementation(testFixtures(project(":common:time")))
    integTestImplementation(testFixtures(project(":common:statsd")))

    gradleTestImplementation(project(":gradle:test-project"))
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

kotlin {
    explicitApi()
}

tasks.named<Test>("integrationTest").configure {
    applyOptionalSystemProperty("avito.slack.test.channelId")
    applyOptionalSystemProperty("avito.slack.test.channel")
    applyOptionalSystemProperty("avito.slack.test.token")
    applyOptionalSystemProperty("avito.slack.test.workspace")
}
