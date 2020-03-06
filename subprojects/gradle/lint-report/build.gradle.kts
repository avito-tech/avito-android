plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.gradle.androidPlugin)
    implementation(Dependencies.kotlinHtml)
    implementation(Dependencies.okhttp)

    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:gradle:impact-shared"))
    implementation(project(":subprojects:common:sentry"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:bitbucket"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))

    testImplementation(testFixtures(project(":subprojects:gradle:logging")))
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
