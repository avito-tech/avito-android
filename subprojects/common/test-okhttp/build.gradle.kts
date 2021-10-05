plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(libs.okhttpMockWebServer)
    api(projects.logger.logger)
    api(projects.common.okhttp)

    implementation(libs.bundles.hamcrest)
    implementation(libs.truth)
    implementation(libs.kotson)
    implementation(libs.commonsLang)

    implementation(projects.common.junitUtils)
    implementation(projects.common.resources)
    implementation(projects.common.waiter)
    implementation(projects.common.result)

    testImplementation(testFixtures(projects.logger.logger))
}
