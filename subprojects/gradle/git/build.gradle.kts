plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.subprojects.gradle.process)
    api(projects.subprojects.common.result)

    implementation(gradleApi())
    implementation(projects.subprojects.gradle.gradleExtensions)

    testImplementation(projects.subprojects.gradle.testProject)
    testImplementation(testFixtures(projects.subprojects.logger.logger))
    testImplementation(libs.mockitoJUnitJupiter)
}
