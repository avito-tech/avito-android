plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    api(libs.okhttpMockWebServer)
    api(projects.common.logger)
    api(projects.common.okhttp)

    implementation(libs.truth)
    implementation(libs.kotson)
    implementation(libs.commonsLang)

    implementation(projects.common.junitUtils)
    implementation(projects.common.resources)
    implementation(projects.common.waiter)
    implementation(projects.common.result)

    testImplementation(testFixtures(projects.common.logger))
}

kotlin {
    explicitApi()
}
