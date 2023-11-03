plugins {
    id("convention.kotlin-jvm-android")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

publish {
    artifactId.set("report-api")
}

dependencies {
    api(projects.subprojects.common.result)
    api(projects.subprojects.common.time)
    api(projects.subprojects.logger.logger)
    api(projects.subprojects.testRunner.reportViewerModel)

    implementation(projects.subprojects.common.okhttp) {
        because("Result extension used")
    }

    testImplementation(projects.subprojects.common.truthExtensions)
    testImplementation(testFixtures(projects.subprojects.common.time))
}
