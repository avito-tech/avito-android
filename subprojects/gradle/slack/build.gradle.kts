import com.avito.kotlin.dsl.getOptionalStringProperty

buildscript {
    val infraVersion: String by project
    dependencies {
        classpath("${Dependencies.gradle.avito.kotlinDslSupport}:$infraVersion")
    }
}

plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
    id("nebula.integtest")
}

dependencies {
    api(Dependencies.funktionaleTry)

    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:common:time"))
    implementation(Dependencies.slackClient) { exclude(group = "com.squareup.okhttp3") }
    implementation(Dependencies.okhttp)
    implementation(Dependencies.coroutinesCore)

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:gradle:slack-test-fixtures"))
    testImplementation(project(":subprojects:common:time-test-fixtures"))
}

tasks.withType(Test::class.java).forEach { testTask ->
    with(testTask) {
        val testProperties = listOf(
            "avito.slack.test.channel",
            "avito.slack.test.token",
            "avito.slack.test.workspace"
        )
        testProperties.forEach { key ->
            systemProperty(key, project.getOptionalStringProperty(key) ?: "")
        }
    }
}
