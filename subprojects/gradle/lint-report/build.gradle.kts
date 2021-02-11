plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
    id("com.jfrog.bintray")
    id("nebula.integtest")
}

dependencies {
    api(project(":subprojects:gradle:build-verdict-tasks-api"))

    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.kotlinHtml)
    implementation(Dependencies.okhttp)

    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:common:sentry"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:bitbucket"))
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:impact-shared"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:slack"))

    testImplementation(testFixtures(project(":subprojects:common:logger")))
    testImplementation(project(":subprojects:gradle:test-project"))
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
