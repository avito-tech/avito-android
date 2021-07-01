plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(libs.okhttp)

    implementation(projects.common.time)
    implementation(projects.common.okhttp)
    implementation(projects.logger.slf4jLogger)

    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)

    testImplementation(projects.common.testOkhttp)
    testImplementation(testFixtures(projects.common.time))
    testImplementation(testFixtures(projects.logger.logger))
}

kotlin {
    explicitApi()
}
