plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
    id("convention.test-fixtures")
}

publish {
    artifactId.set("runner-report")
}

dependencies {
    api(projects.common.reportViewer)

    implementation(projects.common.time)
    implementation(projects.common.httpClient)

    testFixturesImplementation(testFixtures(projects.common.logger))
    testFixturesImplementation(testFixtures(projects.common.time))
    testFixturesImplementation(testFixtures(projects.common.reportViewer))
}

kotlin {
    explicitApi()
}
