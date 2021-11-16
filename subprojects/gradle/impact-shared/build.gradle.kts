plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(projects.subprojects.gradle.moduleDependenciesGraph)

    implementation(gradleApi())

    implementation(projects.subprojects.gradle.android)
    implementation(projects.subprojects.gradle.git)
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.gradle.process)
    implementation(projects.subprojects.logger.gradleLogger)

    implementation(libs.antPattern)
    implementation(libs.kotlinGradle)

    testImplementation(libs.mockitoKotlin)
    testImplementation(projects.subprojects.common.truthExtensions)
    testImplementation(projects.subprojects.gradle.testProject)
    testImplementation(testFixtures(projects.subprojects.gradle.git))
    testImplementation(testFixtures(projects.subprojects.logger.logger))

    testFixturesApi(projects.subprojects.common.result)
    testFixturesApi(projects.subprojects.gradle.testProject)
    testFixturesImplementation(libs.truth)
}
