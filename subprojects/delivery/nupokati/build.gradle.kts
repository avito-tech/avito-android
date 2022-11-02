plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.serialization")
}

dependencies {
    implementation(libs.androidGradle)
    implementation(libs.okhttp)

    implementation(projects.subprojects.delivery.qapps)
    implementation(projects.subprojects.common.okhttp)
    implementation(projects.subprojects.common.problem)
    implementation(projects.subprojects.logger.gradleLogger)
    implementation(projects.subprojects.gradle.git)
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.gradle.statsdConfig)
    implementation(projects.subprojects.logger.slf4jGradleLogger)
    implementation(projects.subprojects.testRunner.instrumentationTests)
    implementation(projects.subprojects.testRunner.reportViewer)

    testImplementation(projects.subprojects.common.truthExtensions)
    testImplementation(projects.subprojects.common.resources)
    testImplementation(projects.subprojects.common.testOkhttp)
    testImplementation(libs.jsonAssert)

    gradleTestImplementation(projects.subprojects.gradle.testProject)
    gradleTestImplementation(projects.subprojects.common.testOkhttp)
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
