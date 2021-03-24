plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    implementation(gradleApi())

    implementation(project(":common:http-client"))
    implementation(project(":common:okhttp"))
    implementation(project(":gradle:build-environment"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":gradle:impact-shared"))
    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
    implementation(libs.sentry)

    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":common:test-okhttp"))
    testImplementation(testFixtures(project(":common:logger")))
    testImplementation(testFixtures(project(":common:http-client")))
}
