plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.test-fixtures")
}

dependencies {
    implementation(projects.subprojects.common.problem)
    implementation(projects.subprojects.delivery.legacySigner)
    implementation(projects.subprojects.delivery.uploadCdBuildResult)
    implementation(projects.subprojects.gradle.android)
    implementation(projects.subprojects.gradle.gradleExtensions)

    gradleTestImplementation(projects.subprojects.gradle.testProject)
    gradleTestImplementation(projects.subprojects.common.testOkhttp)
    gradleTestImplementation(testFixtures(projects.subprojects.logger.logger))

    testFixturesApi(libs.okhttpMockWebServer)
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
