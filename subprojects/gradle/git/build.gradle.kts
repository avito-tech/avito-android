plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(projects.subprojects.gradle.process)
    api(projects.subprojects.common.result)

    implementation(gradleApi())
    implementation(projects.subprojects.gradle.gradleExtensions)

    testImplementation(libs.mockitoJUnitJupiter)
    testImplementation(projects.subprojects.gradle.testProject)
    testImplementation(testFixtures(projects.subprojects.logger.logger))
}
