plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.subprojects.emcee.shared)
    implementation(projects.subprojects.emcee.queueBackendApi)
    implementation(projects.subprojects.testRunner.instrumentationTestsDexLoader)
    implementation(projects.subprojects.testRunner.testAnnotations)
    implementation(libs.coroutinesCore)
}
