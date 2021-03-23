plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
    id("convention.test-fixtures")
}

dependencies {
    implementation(project(":common:okhttp"))
    implementation(project(":common:statsd"))
    implementation(project(":common:logger"))

    testImplementation(project(":common:test-okhttp"))
    testImplementation(project(":common:truth-extensions"))
    testImplementation(testFixtures(project(":common:statsd")))
    testImplementation(testFixtures(project(":common:logger")))

    testFixturesImplementation(testFixtures(project(":common:statsd")))
}

kotlin {
    explicitApi()
}
