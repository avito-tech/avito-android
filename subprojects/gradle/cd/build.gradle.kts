plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    implementation(gradleApi())
    implementation(projects.common.reportViewer)
    implementation(projects.gradle.android)
    implementation(projects.gradle.artifactoryAppBackup)
    implementation(projects.gradle.gradleLogger)
    implementation(projects.common.files)
    implementation(projects.gradle.buildEnvironment)
    implementation(projects.gradle.git)
    implementation(projects.gradle.impact)
    implementation(projects.gradle.impactShared)
    implementation(projects.gradle.instrumentationTests)
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.gradle.lintReport)
    implementation(projects.gradle.prosector)
    implementation(projects.gradle.qapps)
    implementation(projects.gradle.signer)
    implementation(projects.gradle.slack)
    implementation(projects.gradle.teamcity)
    implementation(projects.gradle.testSummary)
    implementation(projects.gradle.tms)
    implementation(projects.gradle.uploadCdBuildResult)
    implementation(projects.gradle.uploadToGoogleplay)

    gradleTestImplementation(projects.common.testOkhttp)
    gradleTestImplementation(projects.gradle.artifactoryAppBackupTestFixtures)
    gradleTestImplementation(projects.gradle.testProject)
    gradleTestImplementation(testFixtures(projects.common.logger))
}

gradlePlugin {
    plugins {
        create("cicd") {
            id = "com.avito.android.cd" // todo rename to ci-steps
            implementationClass = "com.avito.ci.CiStepsPlugin"
            displayName = "CI/CD"
        }
    }
}
