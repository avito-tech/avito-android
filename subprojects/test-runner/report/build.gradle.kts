plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

publish {
    artifactId.set("runner-report")
}

dependencies {
    api(project(":test-runner:report-viewer"))
    api(project(":test-runner:report-api"))

    implementation(project(":common:time"))
    implementation(project(":common:http-client"))

    testImplementation(testFixtures(project(":test-runner:report-api")))

    testFixturesImplementation(testFixtures(project(":logger:logger")))
    testFixturesImplementation(testFixtures(project(":common:time")))
    testFixturesImplementation(testFixtures(project(":test-runner:report-viewer")))
}

kotlin {
    explicitApi()
}
