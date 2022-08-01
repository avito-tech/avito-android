plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.ksp")
}

dependencies {
    api(projects.subprojects.emcee.queueClientModels)
    api(libs.moshiAdapters) {
        "used because EmceeConfig serialized to JSON for testing. Replace as soon as possible"
    }

    implementation(projects.subprojects.common.httpClient)
    implementation(projects.subprojects.common.okhttp)
    implementation(projects.subprojects.emcee.queueClientApi)
    implementation(projects.subprojects.logger.slf4jGradleLogger)
    implementation(projects.subprojects.testRunner.instrumentationTestsDexLoader)
    implementation(projects.subprojects.testRunner.testAnnotations)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)

    ksp(libs.moshiCodegen)
}
