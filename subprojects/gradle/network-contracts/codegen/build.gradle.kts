plugins {
    id("convention.kotlin-jvm")
    id("convention.gradle-testing")
    id("convention.test-fixtures")
    id("convention.serialization")
}

dependencies {
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.gradle.mtls)
    implementation(projects.subprojects.common.okhttp)

    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
    implementation(libs.bundles.ktor)

    implementation(projects.subprojects.logger.gradleLogger)

    gradleTestImplementation(libs.jsonAssert)
    gradleTestImplementation(projects.subprojects.gradle.testProject)
}
