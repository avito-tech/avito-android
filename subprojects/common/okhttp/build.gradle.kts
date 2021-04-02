plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    api(libs.okhttp)

    implementation(projects.common.logger)
    implementation(libs.okhttpLogging)

    testImplementation(projects.common.truthExtensions)
    testImplementation(projects.common.testOkhttp)
    testImplementation(projects.common.result)
    testImplementation(testFixtures(projects.common.logger))
    testImplementation(libs.retrofit)
    testImplementation(libs.retrofitConverterGson)
}

kotlin {
    explicitApi()
}
