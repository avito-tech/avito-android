plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(project(":test-runner:report-api"))
    api(project(":common:result"))
}

kotlin {
    explicitApi()
}
