plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.gradle-testing")
}

dependencies {
    implementation(gradleApi())

    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:gradle:build-environment"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:impact-shared"))
    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    implementation(libs.okhttpLogging)
    implementation(libs.sentry)

    gradleTestImplementation(project(":subprojects:gradle:test-project"))
    gradleTestImplementation(project(":subprojects:common:test-okhttp"))
    gradleTestImplementation(project(":subprojects:common:truth-extensions"))
}
