plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    api(projects.common.logger)

    implementation(libs.slf4jApi)
}
