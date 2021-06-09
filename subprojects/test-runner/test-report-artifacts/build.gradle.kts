plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    api(project(":test-runner:report-api"))
    api(project(":common:result"))
}

kotlin {
    explicitApi()
}
