plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    implementation(gradleApi())

    implementation(project(":gradle:gradle-logger"))
    implementation(project(":common:okhttp"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:impact-shared"))
    implementation(project(":gradle:build-environment"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
    implementation(libs.sentry)

    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":common:test-okhttp"))
    testImplementation(testFixtures(project(":common:logger")))
}
