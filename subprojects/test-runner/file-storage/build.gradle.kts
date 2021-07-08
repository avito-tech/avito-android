plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.common.result)
    api(projects.common.httpClient)

    implementation(libs.retrofit)
    implementation(libs.okhttp)
}
