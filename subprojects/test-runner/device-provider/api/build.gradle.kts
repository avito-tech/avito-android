plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    api(project(":test-runner:device-provider:model"))
}

kotlin {
    explicitApi()
}
