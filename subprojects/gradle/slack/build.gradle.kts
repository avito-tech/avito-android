plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.integration-testing")
    id("convention.libraries")
}

dependencies {
    api(libs.funktionaleTry)

    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:common:time"))
    implementation(libs.slackClient) { exclude(group = "com.squareup.okhttp3") }
    implementation(libs.okhttp)
    implementation(libs.coroutinesCore)

    integTestImplementation(project(":subprojects:common:truth-extensions"))
    integTestImplementation(project(":subprojects:gradle:gradle-extensions"))

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:gradle:slack-test-fixtures"))
    testImplementation(testFixtures(project(":subprojects:common:time")))
    testImplementation(testFixtures(project(":subprojects:common:logger")))
}

tasks.named<Test>("integrationTest").configure {
    applyOptionalSystemProperty("avito.slack.test.channel")
    applyOptionalSystemProperty("avito.slack.test.token")
    applyOptionalSystemProperty("avito.slack.test.workspace")
}

fun Test.applyOptionalSystemProperty(name: String) {
    if(project.hasProperty(name)) {
        project.property(name)?.toString()?.let { value -> systemProperty(name, value) }
    }
}
