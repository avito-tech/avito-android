plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.integration-testing")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    api(project(":subprojects:gradle:build-verdict-tasks-api"))

    implementation(libs.funktionaleTry)
    implementation(libs.kotlinHtml)
    implementation(libs.okhttp)

    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:common:sentry"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:bitbucket"))
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:impact-shared"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:slack"))

    gradleTestImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:common:truth-extensions"))
    testImplementation(testFixtures(project(":subprojects:common:logger")))
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
