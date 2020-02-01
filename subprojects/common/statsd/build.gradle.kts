plugins {
    id("kotlin")
    `maven-publish`
}

val statsdVersion: String by project

dependencies {
    implementation("com.timgroup:java-statsd-client:$statsdVersion")
}
