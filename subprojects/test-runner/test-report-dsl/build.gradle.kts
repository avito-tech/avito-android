plugins {
    id("convention.kotlin-jvm-android")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.subprojects.testRunner.testReportApi)
    api(projects.subprojects.testRunner.testReportDslApi)
    api(projects.subprojects.logger.logger)
    api(libs.junit)
}
