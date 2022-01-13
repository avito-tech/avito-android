plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.test-fixtures")
}

dependencies {
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.androidGradle)

    implementation(projects.subprojects.common.httpClient)
    implementation(projects.subprojects.common.okhttp)
    implementation(projects.subprojects.common.problem)
    implementation(projects.subprojects.delivery.uploadToGoogleplay)
    implementation(projects.subprojects.gradle.git)
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.gradle.statsdConfig)
    implementation(projects.subprojects.logger.slf4jGradleLogger)
    implementation(projects.subprojects.testRunner.instrumentationTests)
    implementation(projects.subprojects.testRunner.reportViewer)

    gradleTestImplementation(projects.subprojects.gradle.testProject)
}

gradlePlugin {
    plugins {
        create("nupokati") {
            id = "com.avito.android.nupokati"
            implementationClass = "com.avito.android.NupokatiPlugin"
            displayName = "Nupokati service plugin"
        }
    }
}
