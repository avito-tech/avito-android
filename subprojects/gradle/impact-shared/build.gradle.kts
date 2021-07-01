plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(projects.gradle.moduleDependenciesGraph)

    implementation(gradleApi())

    implementation(projects.gradle.android)
    implementation(projects.logger.gradleLogger)
    implementation(projects.gradle.git)
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.gradle.process)

    implementation(libs.antPattern)
    implementation(libs.kotlinPlugin)

    testImplementation(projects.common.truthExtensions)
    testImplementation(projects.gradle.gitTestFixtures)
    testImplementation(projects.gradle.testProject)
    testImplementation(testFixtures(projects.logger.logger))

    testImplementation(libs.mockitoKotlin)
}
