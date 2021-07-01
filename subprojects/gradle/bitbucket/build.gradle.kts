plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.gradle-testing")
}

dependencies {
    implementation(gradleApi())

    implementation(projects.common.httpClient)
    implementation(projects.common.okhttp)
    implementation(projects.gradle.buildEnvironment)
    implementation(projects.gradle.git)
    implementation(projects.gradle.git)
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.logger.gradleLogger)
    implementation(projects.gradle.impactShared)
    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
    implementation(libs.sentry)

    gradleTestImplementation(projects.gradle.testProject)
    gradleTestImplementation(projects.common.testOkhttp)
    gradleTestImplementation(projects.common.truthExtensions)
    gradleTestImplementation(testFixtures(projects.logger.logger))
    gradleTestImplementation(testFixtures(projects.common.httpClient))
}
