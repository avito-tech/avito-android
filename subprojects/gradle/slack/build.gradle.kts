import com.avito.android.test.applyOptionalSystemProperty

plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.integration-testing")
    id("convention.libraries")
}

dependencies {
    api(projects.common.result)

    implementation(projects.gradle.gradleLogger)
    implementation(projects.common.time)
    implementation(libs.slackClient) { exclude(group = "com.squareup.okhttp3") }
    implementation(libs.okhttp)
    implementation(libs.coroutinesCore)

    integTestImplementation(projects.common.truthExtensions)
    integTestImplementation(projects.gradle.gradleExtensions)

    testImplementation(projects.gradle.testProject)
    testImplementation(projects.gradle.slackTestFixtures)
    testImplementation(testFixtures(projects.common.time))
    testImplementation(testFixtures(projects.common.logger))
}

tasks.named<Test>("integrationTest").configure {
    applyOptionalSystemProperty("avito.slack.test.channelId")
    applyOptionalSystemProperty("avito.slack.test.channel")
    applyOptionalSystemProperty("avito.slack.test.token")
    applyOptionalSystemProperty("avito.slack.test.workspace")
}
