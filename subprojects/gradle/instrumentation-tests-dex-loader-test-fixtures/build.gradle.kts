plugins {
    id("convention.kotlin-jvm")
}

dependencies {
    api(project(":gradle:instrumentation-tests-dex-loader"))
    api(testFixtures(project(":common:report-viewer")))

    implementation(libs.kotlinStdlib)
}
