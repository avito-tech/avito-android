plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    implementation(gradleApi())
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
    implementation(libs.gson)

    implementation(project(":common:okhttp"))
    implementation(project(":gradle:android"))
    implementation(project(":logger:gradle-logger"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:build-environment"))
    implementation(project(":gradle:gradle-extensions"))

    testImplementation(project(":common:test-okhttp"))
    testImplementation(testFixtures(project(":logger:logger")))
    testImplementation(project(":gradle:git-test-fixtures"))
    testImplementation(project(":gradle:test-project"))
}
