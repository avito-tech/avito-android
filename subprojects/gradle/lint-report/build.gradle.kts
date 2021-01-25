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
    implementation(Dependencies.kotlinHtml)
    implementation(Dependencies.okhttp)

    implementation(project(":common:okhttp"))
    implementation(project(":common:sentry"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:bitbucket"))
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:impact-shared"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:slack"))

    testImplementation(project(":common:logger-test-fixtures"))
    testImplementation(project(":gradle:test-project"))
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
