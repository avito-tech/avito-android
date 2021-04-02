plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(libs.kotlinStdlib)
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:signer"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:upload-cd-build-result"))

    testImplementation(project(":gradle:artifactory-app-backup-test-fixtures"))

    gradleTestImplementation(project(":gradle:test-project"))
    gradleTestImplementation(project(":common:test-okhttp"))
    gradleTestImplementation(project(":gradle:artifactory-app-backup-test-fixtures"))
    gradleTestImplementation(testFixtures(project(":common:logger")))
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
