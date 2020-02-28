plugins {
    id("kotlin")
    `maven-publish`
    id("java-test-fixtures")
    id("com.jfrog.bintray")
}

val kotlinVersion: String by project
val statsdVersion: String by project

dependencies {
    implementation("com.timgroup:java-statsd-client:$statsdVersion")

    testFixturesImplementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}
