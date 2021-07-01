plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.logger.logger)

    implementation(libs.slf4jApi)
}
