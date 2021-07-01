import com.avito.android.test.applyOptionalSystemProperty

plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.integration-testing")
}

dependencies {
    api(projects.common.result)

    implementation(projects.logger.gradleLogger)
    implementation(projects.common.time)
    implementation(projects.common.httpClient)
    implementation(projects.common.okhttp)
    implementation(libs.slackClient) { exclude(group = "com.squareup.okhttp3") }
    implementation(libs.okhttp)
    implementation(libs.coroutinesCore)

    integTestImplementation(projects.common.truthExtensions)
    integTestImplementation(projects.gradle.gradleExtensions)
    integTestImplementation(testFixtures(projects.common.httpClient))

    testImplementation(projects.gradle.testProject)
    testImplementation(projects.gradle.slackTestFixtures)
    testImplementation(testFixtures(projects.common.time))
    testImplementation(testFixtures(projects.logger.logger))
    testImplementation(testFixtures(projects.common.statsd))
}

tasks.named<Test>("integrationTest").configure {
    applyOptionalSystemProperty("avito.slack.test.channelId")
    applyOptionalSystemProperty("avito.slack.test.channel")
    applyOptionalSystemProperty("avito.slack.test.token")
    applyOptionalSystemProperty("avito.slack.test.workspace")
}
