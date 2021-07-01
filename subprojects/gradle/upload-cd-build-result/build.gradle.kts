plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    implementation(gradleApi())
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
    implementation(libs.gson)

    implementation(projects.common.okhttp)
    implementation(projects.gradle.android)
    implementation(projects.logger.gradleLogger)
    implementation(projects.gradle.git)
    implementation(projects.gradle.buildEnvironment)
    implementation(projects.gradle.gradleExtensions)

    testImplementation(projects.common.testOkhttp)
    testImplementation(testFixtures(projects.logger.logger))
    testImplementation(projects.gradle.gitTestFixtures)
    testImplementation(projects.gradle.testProject)
}
