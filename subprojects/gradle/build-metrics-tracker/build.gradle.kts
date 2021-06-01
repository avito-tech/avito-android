plugins {
    id("convention.kotlin-jvm")
    id("convention.libraries")
}

dependencies {
    api(project(":common:statsd"))
    implementation(project(":gradle:build-environment"))

    testImplementation(testFixtures(project(":common:graphite")))
    testImplementation(testFixtures(project(":common:statsd")))
    testImplementation(testFixtures(project(":gradle:build-environment")))
}

kotlin {
    explicitApi()
}
