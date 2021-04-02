plugins {
    id("convention.kotlin-jvm")
}

dependencies {
    api(projects.gradle.instrumentationTestsDexLoader)
    api(testFixtures(projects.common.reportViewer))
}
