plugins {
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project
val sentryVersion: String by project

dependencies {
    api("io.sentry:sentry:$sentryVersion")

    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation(project(":utils"))
    implementation(project(":git"))
    implementation(project(":kotlin-dsl-support"))
    // can't use logging due to cyclic dependency
}

