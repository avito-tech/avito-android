plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
    id("convention.test-fixtures")
}

publish {
    artifactId = "runner-report"
}

dependencies {
    api(project(":subprojects:common:report-viewer"))

    implementation(project(":subprojects:common:time"))
    implementation(testFixtures(project(":subprojects:common:report-viewer")))

    testFixturesImplementation(testFixtures(project(":subprojects:common:logger")))
    testFixturesImplementation(testFixtures(project(":subprojects:common:time")))
    testFixturesImplementation(testFixtures(project(":subprojects:common:report-viewer")))
}

kotlin {
    explicitApi()
}
