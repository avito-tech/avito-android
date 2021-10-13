plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.gradle.process)
    api(projects.common.result)

    implementation(gradleApi())
    implementation(projects.gradle.gradleExtensions)

    testImplementation(projects.gradle.testProject)
    testImplementation(testFixtures(projects.logger.logger))
    testImplementation(libs.mockitoJUnitJupiter)
}
