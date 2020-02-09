plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

val sentryVersion: String by project

dependencies {
    api("io.sentry:sentry:$sentryVersion")
}
