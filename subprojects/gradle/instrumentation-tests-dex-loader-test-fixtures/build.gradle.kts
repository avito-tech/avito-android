plugins {
    id("kotlin")
}

dependencies {
    api(project(":subprojects:gradle:instrumentation-tests-dex-loader"))
    api(project(":subprojects:common:report-viewer-test-fixtures"))
}
