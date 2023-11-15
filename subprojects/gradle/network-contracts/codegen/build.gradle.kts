plugins {
    id("convention.kotlin-jvm")
    id("convention.gradle-testing")
    id("convention.test-fixtures")
    id("convention.serialization")
}

dependencies {
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.common.okhttp)

    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)

    implementation(projects.subprojects.logger.gradleLogger)

    gradleTestImplementation(libs.jsonAssert)
    gradleTestImplementation(projects.subprojects.gradle.testProject)
}
