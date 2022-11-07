plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.subprojects.common.result)
    api(projects.subprojects.common.httpStatsd)
    implementation(libs.retrofit)
}
