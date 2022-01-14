plugins {
    id("convention.kotlin-jvm")
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
    api(libs.okhttp) {
        because("HttpUrl used to type urls more strict")
    }
    api(libs.gson) {
        // todo hide parsing
        // todo replace JsonElement with something more generic if possible
        because("module provides TypeAdapterFactory for Entries; JsonElement in the IncidentElement")
    }
    api(projects.subprojects.testRunner.testModel)

    implementation(projects.subprojects.common.okhttp) {
        because("Result extension used")
    }

    testImplementation(projects.subprojects.common.truthExtensions)
    testImplementation(testFixtures(projects.subprojects.common.time))
}
