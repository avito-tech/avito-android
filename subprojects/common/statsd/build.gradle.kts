plugins {
    id("convention.kotlin-jvm-android")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(projects.subprojects.common.series)
    implementation(libs.statsd)
    implementation(projects.subprojects.logger.logger)
}
