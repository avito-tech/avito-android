plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.common.result)
    api(projects.common.httpClient)

    implementation(projects.common.time)
    implementation(projects.logger.logger)
    implementation(projects.common.okhttp)

    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
}
