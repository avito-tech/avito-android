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
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
}

