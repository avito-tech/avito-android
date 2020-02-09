plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

val statsdVersion: String by project

dependencies {
    implementation("com.timgroup:java-statsd-client:$statsdVersion")
}
