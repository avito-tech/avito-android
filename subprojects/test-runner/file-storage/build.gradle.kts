plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.common.result)

    implementation(projects.common.time)
    implementation(projects.logger.logger)
    implementation(projects.common.okhttp)
    implementation(projects.common.httpClient)

    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
}
