plugins {
    id("convention.kotlin-jvm")
}

dependencies {
    api(project(":test-runner:instrumentation-tests-dex-loader"))
    api(testFixtures(project(":common:report-viewer")))
}
