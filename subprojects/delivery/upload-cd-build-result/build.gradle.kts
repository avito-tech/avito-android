plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    implementation(gradleApi())
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
    implementation(libs.gson)

    implementation(projects.subprojects.common.okhttp)
    implementation(projects.subprojects.gradle.android)
    implementation(projects.subprojects.logger.gradleLogger)
    implementation(projects.subprojects.gradle.git)
    implementation(projects.subprojects.gradle.buildEnvironment)
    implementation(projects.subprojects.gradle.gradleExtensions)

    testImplementation(projects.subprojects.common.testOkhttp)
    testImplementation(projects.subprojects.gradle.testProject)
    testImplementation(testFixtures(projects.subprojects.gradle.git))
    testImplementation(testFixtures(projects.subprojects.logger.logger))
}
