plugins {
    id("kotlin")
}

dependencies {
    api(project(":subprojects:gradle:instrumentation-tests-dex-loader"))
    api(testFixtures(project(":subprojects:common:report-viewer")))
}
