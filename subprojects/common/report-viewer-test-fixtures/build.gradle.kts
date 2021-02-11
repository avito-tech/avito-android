plugins {
    id("kotlin")
}

dependencies {
    api(project(":subprojects:common:report-viewer"))

    implementation(project(":subprojects:common:test-okhttp"))
    implementation(testFixtures(project(":subprojects:common:logger")))

    implementation(Dependencies.Test.junitJupiterApi)
}
