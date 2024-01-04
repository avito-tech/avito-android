plugins {
    id("convention.kotlin-jvm-android")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

publish {
    artifactId.set("report-api")
}

dependencies {
    api(project(":subprojects:common:result"))
    api(project(":subprojects:common:time"))
    api(project(":subprojects:logger:logger"))
    api(project(":subprojects:test-runner:report-viewer-model"))

    implementation(project(":subprojects:common:okhttp")) {
        because("Result extension used")
    }

    testImplementation(project(":subprojects:common:truth-extensions"))
    testImplementation(testFixtures(project(":subprojects:common:time")))
}
