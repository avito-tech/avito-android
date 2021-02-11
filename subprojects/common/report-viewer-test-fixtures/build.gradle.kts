plugins {
    id("kotlin")
}

dependencies {
    api(project(":subprojects:common:report-viewer"))

    implementation(project(":subprojects:common:test-okhttp"))
    implementation(project(":subprojects:common:logger-test-fixtures"))

    implementation(Dependencies.Test.junitJupiterApi)
}
