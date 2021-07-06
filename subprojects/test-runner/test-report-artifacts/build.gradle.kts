plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.testRunner.report)
    api(projects.common.result)
}
