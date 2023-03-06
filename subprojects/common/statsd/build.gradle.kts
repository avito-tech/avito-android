plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(projects.subprojects.common.series)
    implementation(libs.statsd)
    implementation(projects.subprojects.logger.logger)
}
