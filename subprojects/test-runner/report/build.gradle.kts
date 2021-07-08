plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

publish {
    artifactId.set("report-api")
}

dependencies {
    api(projects.common.result)
    api(projects.common.time)
    api(projects.logger.logger)
    api(libs.okhttp) {
        because("HttpUrl used to type urls more strict")
    }
    api(libs.gson) {
        // todo hide parsing
        // todo replace JsonElement with something more generic if possible
        because("module provides TypeAdapterFactory for Entries; JsonElement in the IncidentElement")
    }
    api(projects.testRunner.testModel)

    implementation(projects.common.okhttp) {
        because("Result extension used")
    }

    testImplementation(projects.common.truthExtensions)
    testImplementation(testFixtures(projects.logger.logger))
    testImplementation(testFixtures(projects.common.time))
}
