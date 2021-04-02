plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
    id("convention.test-fixtures")
}

dependencies {
    implementation(libs.statsd)
    implementation(projects.common.logger)
}
