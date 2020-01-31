plugins {
    id("kotlin")
    `maven-publish`
}

val sentryVersion: String by project
val funktionaleVersion: String by project

dependencies {
    api("io.sentry:sentry:$sentryVersion")

    implementation(gradleApi())
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")
    implementation(project(":utils"))
    implementation(project(":git"))
    implementation(project(":kotlin-dsl-support"))
}

