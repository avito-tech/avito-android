plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    implementation(gradleApi())

    implementation(project(":common:http-client"))
    implementation(project(":common:okhttp"))
    implementation(project(":gradle:build-environment"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":logger:gradle-logger"))
    implementation(project(":gradle:impact-shared"))
    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
    implementation(libs.sentry)

    gradleTestImplementation(project(":gradle:test-project"))
    gradleTestImplementation(project(":common:test-okhttp"))
    gradleTestImplementation(project(":common:truth-extensions"))
    gradleTestImplementation(testFixtures(project(":logger:logger")))
    gradleTestImplementation(testFixtures(project(":common:http-client")))
}
