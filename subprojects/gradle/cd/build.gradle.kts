plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(gradleApi())
    implementation(projects.subprojects.testRunner.reportViewer)
    implementation(projects.subprojects.gradle.android)
    implementation(projects.subprojects.gradle.artifactoryAppBackup)
    implementation(projects.subprojects.logger.gradleLogger)
    implementation(projects.subprojects.common.files)
    implementation(projects.subprojects.common.problem)
    implementation(projects.subprojects.gradle.buildEnvironment)
    implementation(projects.subprojects.gradle.git)
    implementation(projects.subprojects.gradle.impact)
    implementation(projects.subprojects.gradle.impactShared)
    implementation(projects.subprojects.testRunner.instrumentationTests)
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.gradle.prosector)
    implementation(projects.subprojects.gradle.qapps)
    implementation(projects.subprojects.signer)
    implementation(projects.subprojects.gradle.slack)
    implementation(projects.subprojects.gradle.teamcity)
    implementation(projects.subprojects.gradle.testSummary)
    implementation(projects.subprojects.gradle.uploadCdBuildResult)
    implementation(projects.subprojects.gradle.uploadToGoogleplay)
    implementation(projects.subprojects.gradle.statsdConfig)

    gradleTestImplementation(projects.subprojects.common.testOkhttp)
    gradleTestImplementation(projects.subprojects.gradle.artifactoryAppBackupTestFixtures)
    gradleTestImplementation(projects.subprojects.gradle.testProject)
    gradleTestImplementation(testFixtures(projects.subprojects.logger.logger))
}

gradlePlugin {
    plugins {
        create("cicd") {
            id = "com.avito.android.cd"
            implementationClass = "com.avito.ci.CiStepsPlugin"
            displayName = "CI/CD"
        }
    }
}
