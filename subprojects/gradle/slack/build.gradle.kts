import com.avito.android.test.applySystemProperties

plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.integration-testing")
    id("convention.test-fixtures")
}

dependencies {
    api(projects.subprojects.common.result)

    implementation(projects.subprojects.common.time)
    implementation(projects.subprojects.common.httpClient)
    implementation(projects.subprojects.common.okhttp)
    implementation(libs.slackClient) { exclude(group = "com.squareup.okhttp3") }
    implementation(libs.okhttp)
    implementation(libs.coroutinesCore)

    integTestImplementation(projects.subprojects.common.truthExtensions)
    integTestImplementation(projects.subprojects.gradle.gradleExtensions)
    integTestImplementation(testFixtures(projects.subprojects.common.httpClient))

    testImplementation(projects.subprojects.gradle.testProject)
    testImplementation(testFixtures(projects.subprojects.common.statsd))
    testImplementation(testFixtures(projects.subprojects.common.time))
    testImplementation(testFixtures(projects.subprojects.gradle.slack))
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
                "It should be added to <GRADLE_USER_HOME>/gradle.properties"
        }
    }
}
