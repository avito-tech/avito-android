plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    api(project(":logger:logger"))

    implementation(libs.slf4jApi)
}
