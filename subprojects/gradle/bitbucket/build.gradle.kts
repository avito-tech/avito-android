plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    implementation(gradleApi())

    implementation(projects.common.httpClient)
    implementation(projects.common.okhttp)
    implementation(projects.gradle.buildEnvironment)
    implementation(projects.gradle.git)
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.gradle.gradleLogger)
    implementation(projects.gradle.impactShared)
    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
    implementation(libs.sentry)

    testImplementation(projects.gradle.testProject)
    testImplementation(projects.common.testOkhttp)
    testImplementation(projects.common.truthExtensions)
    testImplementation(testFixtures(projects.common.logger))
    testImplementation(testFixtures(projects.common.httpClient))
}
