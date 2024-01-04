plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    implementation(project(":subprojects:test-runner:test-annotations"))
    implementation(project(":subprojects:test-runner:report-viewer-model"))
    implementation(project(":subprojects:test-runner:report-viewer-test-static-data-parser"))
    implementation(project(":subprojects:test-runner:instrumentation-tests-dex-loader"))
    implementation(project(":subprojects:test-runner:test-model"))
    implementation(project(":subprojects:logger:logger"))
    implementation(libs.gson)
    implementation(libs.androidAnnotations)

    testImplementation(project(":subprojects:common:truth-extensions"))
    testImplementation(testFixtures(project(":subprojects:test-runner:instrumentation-tests-dex-loader")))
}
