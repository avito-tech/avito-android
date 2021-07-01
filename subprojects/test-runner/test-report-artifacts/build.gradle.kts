plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.testRunner.reportApi)
    api(projects.common.result)
}

kotlin {
    explicitApi()
}
