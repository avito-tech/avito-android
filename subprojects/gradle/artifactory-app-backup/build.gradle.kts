plugins {
    id("kotlin")
    id("java-gradle-plugin")
    id("java-test-fixtures")
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
    testImplementation(project(":subprojects:common:test-okhttp"))

    testFixturesImplementation(project(":subprojects:gradle:test-project"))
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
