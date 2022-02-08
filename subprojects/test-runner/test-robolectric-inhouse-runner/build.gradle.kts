plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.subprojects.testRunner.testRobolectricRunner)

    implementation(projects.subprojects.logger.logger)
    implementation(projects.subprojects.testRunner.shared.loggerProviders)
}
