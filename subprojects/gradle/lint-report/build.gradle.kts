plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
    id("com.jfrog.bintray")
    id("nebula.integtest")
}

dependencies {
    api(project(":gradle:build-verdict-tasks-api"))

    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.Gradle.androidPlugin)
    implementation(Dependencies.kotlinHtml)
    implementation(Dependencies.okhttp)

    implementation(project(":common:okhttp"))
    implementation(project(":common:sentry"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:bitbucket"))
    implementation(project(":gradle:ci-logger"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:impact-shared"))
    implementation(project(":gradle:kotlin-dsl-support"))
    implementation(project(":gradle:slack"))

    testImplementation(project(":gradle:logging-test-fixtures"))
}

gradlePlugin {
    plugins {
        create("lintReport") {
            id = "com.avito.android.lint-report"
            implementationClass = "com.avito.android.lint.LintReportPlugin"
            displayName = "Lint reports merge"
        }
    }
}

tasks.withType(Test::class.java).forEach { testTask ->
    with(testTask) {
        val testProperties = listOf(
            "avito.slack.test.channel",
            "avito.slack.test.token",
            "avito.slack.test.workspace"
        )
        testProperties.forEach { key ->
            val property = if (project.hasProperty(key)) {
                project.property(key)!!.toString()
            } else {
                ""
            }
            systemProperty(key, property)
        }
    }
}
