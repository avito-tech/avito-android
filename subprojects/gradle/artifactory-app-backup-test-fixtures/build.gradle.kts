plugins {
    id("convention.kotlin-jvm")
}

dependencies {
    api(projects.gradle.artifactoryAppBackup)

    implementation(projects.common.testOkhttp)
}
