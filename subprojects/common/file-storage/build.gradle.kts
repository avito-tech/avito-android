plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    implementation(projects.common.time)
    implementation(projects.common.logger)
    implementation(projects.common.okhttp)
    implementation(projects.common.httpClient)

    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
}
