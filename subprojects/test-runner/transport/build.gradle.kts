plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    implementation(project(":subprojects:test-runner:report"))
    implementation(project(":subprojects:test-runner:test-report"))
    implementation(project(":subprojects:test-runner:test-report-artifacts"))
    implementation(project(":subprojects:test-runner:report-viewer"))
}
