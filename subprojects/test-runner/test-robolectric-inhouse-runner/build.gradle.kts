plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(libs.robolectric)
    api(libs.junit)

    implementation(projects.subprojects.logger.logger)
    implementation(projects.subprojects.testRunner.shared.loggerProviders)
}
