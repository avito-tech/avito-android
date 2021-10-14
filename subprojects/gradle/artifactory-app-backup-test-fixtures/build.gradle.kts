plugins {
    id("convention.kotlin-jvm")
}

dependencies {
    api(projects.subprojects.gradle.artifactoryAppBackup)

    implementation(projects.subprojects.common.testOkhttp)
}
