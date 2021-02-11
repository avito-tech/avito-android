plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:signer"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:upload-cd-build-result"))

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:gradle:artifactory-app-backup-test-fixtures"))
    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(project(":subprojects:common:logger-test-fixtures"))
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
