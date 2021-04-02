plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    api(projects.gradle.process)
    api(projects.common.result)

    implementation(gradleApi())
    implementation(projects.common.logger)
    implementation(projects.common.slf4jLogger)
    implementation(projects.gradle.gradleExtensions)

    testImplementation(projects.gradle.testProject)
    testImplementation(testFixtures(projects.common.logger))
    testImplementation(libs.mockitoJUnitJupiter)
}
