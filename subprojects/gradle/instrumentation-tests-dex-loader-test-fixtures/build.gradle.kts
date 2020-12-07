plugins {
    id("kotlin")
}

dependencies {
    api(project(":gradle:instrumentation-tests-dex-loader"))
    api(project(":common:report-viewer-test-fixtures"))
}
