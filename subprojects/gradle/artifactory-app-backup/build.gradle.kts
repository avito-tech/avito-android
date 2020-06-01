plugins {
    id("kotlin")
    id("java-gradle-plugin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":gradle:kotlin-dsl-support"))
    implementation(project(":gradle:signer"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:upload-cd-build-result"))
    implementation(Dependencies.gradle.androidPlugin)

    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":gradle:artifactory-app-backup-test-fixtures"))
    testImplementation(project(":common:test-okhttp"))
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
