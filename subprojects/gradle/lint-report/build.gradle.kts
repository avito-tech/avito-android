plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":gradle:build-verdict-tasks-api"))

    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.Gradle.androidPlugin)
    implementation(Dependencies.kotlinHtml)
    implementation(Dependencies.okhttp)

    implementation(project(":gradle:android"))
    implementation(project(":gradle:utils"))
    implementation(project(":gradle:ci-logger"))
    implementation(project(":common:okhttp"))
    implementation(project(":gradle:impact-shared"))
    implementation(project(":common:sentry"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:bitbucket"))
    implementation(project(":gradle:kotlin-dsl-support"))

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
