plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.testRunner.testReportApi)
    api(projects.testRunner.testReportDslApi)
    api(projects.logger.logger)
    api(libs.junit)
}
