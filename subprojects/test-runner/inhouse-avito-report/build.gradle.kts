plugins {
    id("convention.kotlin-jvm-android")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(project(":subprojects:common:time"))
    api(project(":subprojects:logger:logger"))
    api(project(":subprojects:test-runner:report"))
    implementation(project(":subprojects:test-runner:report-viewer"))

    testImplementation(project(":subprojects:common:truth-extensions"))
    testImplementation(testFixtures(project(":subprojects:common:time")))
    testImplementation(testFixtures(project(":subprojects:test-runner:report")))
    testImplementation(testFixtures(project(":subprojects:test-runner:report-viewer")))
}
