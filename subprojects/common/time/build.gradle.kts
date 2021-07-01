plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(projects.logger.logger)
}

kotlin {
    explicitApi()
}
