plugins {
    id("kotlin")
}

dependencies {
    api(project(":common:report-viewer"))

    implementation(project(":common:test-okhttp"))
    implementation(project(":common:logger-test-fixtures"))

    implementation(Dependencies.test.junitJupiterApi)
}
