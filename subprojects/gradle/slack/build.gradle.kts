import com.avito.android.test.applyOptionalSystemProperty

plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.integration-testing")
    id("convention.libraries")
}

dependencies {
    api(project(":common:result"))

    implementation(project(":gradle:gradle-logger"))
    implementation(project(":common:time"))
    implementation(libs.slackClient) { exclude(group = "com.squareup.okhttp3") }
    implementation(libs.okhttp)
    implementation(libs.coroutinesCore)

    integTestImplementation(project(":common:truth-extensions"))
    integTestImplementation(project(":gradle:gradle-extensions"))

    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":gradle:slack-test-fixtures"))
    testImplementation(testFixtures(project(":common:time")))
    testImplementation(testFixtures(project(":common:logger")))
}

tasks.named<Test>("integrationTest").configure {
    applyOptionalSystemProperty("avito.slack.test.channel")
    applyOptionalSystemProperty("avito.slack.test.token")
    applyOptionalSystemProperty("avito.slack.test.workspace")
}
