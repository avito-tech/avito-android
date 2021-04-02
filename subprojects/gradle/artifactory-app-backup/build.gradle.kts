plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.gradle.signer)
    implementation(projects.gradle.android)
    implementation(projects.gradle.uploadCdBuildResult)

    testImplementation(projects.gradle.artifactoryAppBackupTestFixtures)

    gradleTestImplementation(projects.gradle.testProject)
    gradleTestImplementation(projects.common.testOkhttp)
    gradleTestImplementation(projects.gradle.artifactoryAppBackupTestFixtures)
    gradleTestImplementation(testFixtures(projects.common.logger))
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
