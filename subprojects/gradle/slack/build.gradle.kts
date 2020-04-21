plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
    id("nebula.integtest")
}

dependencies {
    api(Dependencies.funktionaleTry)

    implementation(project(":gradle:utils"))
    implementation(project(":gradle:logging"))
    implementation(project(":common:time"))
    implementation(Dependencies.slackClient) { exclude(group = "com.squareup.okhttp3") }
    implementation(Dependencies.okhttp)
    implementation(Dependencies.coroutinesCore)

    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":gradle:slack-test-fixtures"))
    testImplementation(project(":common:time-test-fixtures"))
}

tasks.withType(Test::class.java).forEach { testTask ->
    with(testTask) {
        val testProperties = listOf(
            "avito.slack.test.channel",
            "avito.slack.test.token",
            "avito.slack.test.workspace"
        )
//        testProperties.forEach { key ->
//            systemProperty(key, project.getOptionalStringProperty(key) ?: "")
//        }
    }
}
