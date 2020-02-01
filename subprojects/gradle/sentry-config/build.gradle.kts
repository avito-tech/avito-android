plugins {
    id("kotlin")
    `maven-publish`
}

val funktionaleVersion: String by project

dependencies {
    api(project(":subprojects:common:sentry"))

    implementation(gradleApi())
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")
}

