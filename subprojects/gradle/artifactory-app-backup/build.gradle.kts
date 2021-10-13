plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.signer)
    implementation(projects.subprojects.gradle.android)
    implementation(projects.subprojects.gradle.uploadCdBuildResult)
    implementation(projects.subprojects.common.problem)

    testImplementation(projects.subprojects.gradle.artifactoryAppBackupTestFixtures)

    gradleTestImplementation(projects.subprojects.gradle.testProject)
    gradleTestImplementation(projects.subprojects.common.testOkhttp)
    gradleTestImplementation(projects.subprojects.gradle.artifactoryAppBackupTestFixtures)
    gradleTestImplementation(testFixtures(projects.subprojects.logger.logger))
}

gradlePlugin {
    plugins {
        create("artifactory") {
            id = "com.avito.android.artifactory-app-backup"
            implementationClass = "com.avito.android.plugin.artifactory.ArtifactoryAppBackupPlugin"
            displayName = "Artifactory backup"
        }
    }
}
