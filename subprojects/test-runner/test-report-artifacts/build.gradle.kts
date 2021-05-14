plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    api(project(":common:report-api"))
    api(project(":common:result"))
}

kotlin {
    explicitApi()
}
