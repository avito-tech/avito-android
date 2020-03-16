import com.avito.kotlin.dsl.getOptionalStringProperty

buildscript {
    val infraVersion: String by project
    dependencies {
        classpath("${Dependencies.gradle.avito.kotlinDslSupport}:$infraVersion")
    }
}

plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
    id("com.jfrog.bintray")
    id("nebula.integtest")
}

dependencies {
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:common:time"))
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.slackClient) { exclude(group = "com.squareup.okhttp3") }
    implementation(Dependencies.okhttp)
    implementation(Dependencies.coroutinesCore)

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(testFixtures(project(":subprojects:common:time")))

    testFixturesImplementation(Dependencies.kotlinStdlib)
    testFixturesImplementation(Dependencies.funktionaleTry)
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