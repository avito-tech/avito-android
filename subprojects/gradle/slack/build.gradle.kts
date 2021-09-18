import com.avito.android.test.applySystemProperties

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

    applySystemProperties(
        "avito.slack.test.channelId",
        "avito.slack.test.channel",
        "avito.slack.test.token",
        "avito.slack.test.workspace"
    ) { missing ->
        require(missing.isEmpty()) {
            "$path:integrationTest requires additional properties to be applied\n" +
                "missing values are: $missing\n" +
                "It should be added to ~/.gradle/gradle.properties"
        }
    }
}
