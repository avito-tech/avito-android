plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

publish {
    artifactId.set("runner-report")
}

dependencies {
    api(projects.testRunner.reportViewer)
    api(projects.testRunner.reportApi)

    implementation(projects.common.time)
    implementation(projects.common.httpClient)

    testImplementation(testFixtures(projects.testRunner.reportApi))

    testFixturesImplementation(testFixtures(projects.logger.logger))
    testFixturesImplementation(testFixtures(projects.common.time))
    testFixturesImplementation(testFixtures(projects.testRunner.reportViewer))
}

kotlin {
    explicitApi()
}
