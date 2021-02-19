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
