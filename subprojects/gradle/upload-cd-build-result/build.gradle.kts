plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    implementation(gradleApi())
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
    implementation(libs.gson)

    implementation(project(":common:okhttp"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:build-environment"))
    implementation(project(":gradle:gradle-extensions"))

    testImplementation(project(":common:test-okhttp"))
    testImplementation(testFixtures(project(":common:logger")))
    testImplementation(project(":gradle:git-test-fixtures"))
    testImplementation(project(":gradle:test-project"))
}
