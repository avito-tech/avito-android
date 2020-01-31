plugins {
    id("kotlin")
    id("java-gradle-plugin")
    id("java-test-fixtures")
    `maven-publish`
}

val androidGradlePluginVersion: String by project

dependencies {
    implementation(project(":kotlin-dsl-support"))
    implementation(project(":signer"))
    implementation(project(":android"))
    implementation(project(":upload-cd-build-result"))
    implementation("com.android.tools.build:gradle:$androidGradlePluginVersion")

    testImplementation(project(":test-project"))
    testImplementation(project(":test-okhttp"))

    testFixturesImplementation(project(":test-project"))
}

gradlePlugin {
    plugins {
        create("artifactory") {
            id = "com.avito.android.artifactory-app-backup"
            implementationClass = "com.avito.android.plugin.artifactory.ArtifactoryAppBackupPlugin"
        }
    }
}
