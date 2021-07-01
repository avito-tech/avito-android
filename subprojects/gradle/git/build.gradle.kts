plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.gradle.process)
    api(projects.common.result)

    implementation(gradleApi())
    implementation(projects.logger.logger)
    implementation(projects.logger.slf4jLogger)
    implementation(projects.gradle.gradleExtensions)

    testImplementation(projects.gradle.testProject)
    testImplementation(testFixtures(projects.logger.logger))
    testImplementation(libs.mockitoJUnitJupiter)
}
