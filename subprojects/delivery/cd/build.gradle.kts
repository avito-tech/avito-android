plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(gradleApi())
    implementation(projects.subprojects.common.files)
    implementation(projects.subprojects.common.problem)
    implementation(projects.subprojects.delivery.artifactoryAppBackup)
    implementation(projects.subprojects.delivery.qapps)
    implementation(projects.subprojects.delivery.legacySigner)
    implementation(projects.subprojects.delivery.uploadCdBuildResult)
    implementation(projects.subprojects.delivery.uploadToGoogleplay)
    implementation(projects.subprojects.gradle.android)
    implementation(projects.subprojects.gradle.buildEnvironment)
    implementation(projects.subprojects.gradle.git)
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.gradle.impact)
    implementation(projects.subprojects.gradle.impactShared)
    implementation(projects.subprojects.gradle.prosector)
    implementation(projects.subprojects.gradle.slack)
    implementation(projects.subprojects.gradle.statsdConfig)
    implementation(projects.subprojects.gradle.teamcity)
    implementation(projects.subprojects.gradle.testSummary)
    implementation(projects.subprojects.testRunner.instrumentationTests)
    implementation(projects.subprojects.testRunner.reportViewer)

    gradleTestImplementation(projects.subprojects.common.testOkhttp)
    gradleTestImplementation(projects.subprojects.gradle.testProject)
    gradleTestImplementation(testFixtures(projects.subprojects.delivery.artifactoryAppBackup))
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
