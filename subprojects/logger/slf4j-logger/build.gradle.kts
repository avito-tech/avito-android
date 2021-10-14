plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.subprojects.logger.logger)

    implementation(libs.slf4jApi)
}
