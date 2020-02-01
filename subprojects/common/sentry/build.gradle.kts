plugins {
    id("kotlin")
    `maven-publish`
}

val sentryVersion: String by project

dependencies {
    api("io.sentry:sentry:$sentryVersion")
}
