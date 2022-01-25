plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(libs.okhttpMockWebServer)
    api(projects.subprojects.common.okhttp)

    implementation(libs.bundles.hamcrest)
    implementation(libs.truth)
    implementation(libs.kotson)
    implementation(libs.commonsLang)

    implementation(projects.subprojects.logger.logger)
    implementation(projects.subprojects.common.junitUtils)
    implementation(projects.subprojects.common.resources)
    implementation(projects.subprojects.common.waiter)
    implementation(projects.subprojects.common.result)

    implementation(libs.jsonAssert)
}
