plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.test-fixtures")
}

dependencies {
    api(projects.subprojects.gradle.codeOwnership)
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.gradle.slack)
    implementation(projects.subprojects.common.httpClient)
    implementation(projects.subprojects.gradle.statsdConfig)
    implementation(projects.subprojects.logger.slf4jGradleLogger)

    gradleTestImplementation(projects.subprojects.gradle.testProject)
}

gradlePlugin {
    plugins {
        create("codeOwnershipReport") {
            id = "com.avito.android.code-ownership-report"
            implementationClass = "com.avito.android.CodeOwnershipReportPlugin"
            displayName = "Code Ownership Report"
        }
    }
}
