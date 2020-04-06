plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:signer"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:upload-cd-build-result"))
    implementation(Dependencies.gradle.androidPlugin)

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:gradle:artifactory-app-backup-test-fixtures"))
    testImplementation(project(":subprojects:common:test-okhttp"))
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
