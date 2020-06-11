plugins {
    id("kotlin")
}

dependencies {
    api(project(":gradle:instrumentation-tests"))
    api(project(":common:report-viewer-test-fixtures"))

    implementation(project(":gradle:bitbucket"))
    implementation(project(":common:report-viewer"))
    implementation(project(":gradle:slack"))
    implementation(project(":gradle:statsd-config"))
    implementation(project(":gradle:kubernetes"))
    implementation(project(":gradle:utils"))
    implementation(project(":gradle:test-project"))
}
