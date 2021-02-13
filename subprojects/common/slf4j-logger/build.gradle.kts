plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    api(project(":subprojects:common:logger"))

    implementation(libs.slf4jApi)
}
