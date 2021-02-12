plugins {
    id("com.avito.android.kotlin-jvm")
}

dependencies {
    api(project(":subprojects:gradle:instrumentation-tests-dex-loader"))
    api(testFixtures(project(":subprojects:common:report-viewer")))
}
