plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

val kotlinVersion: String by project
val funktionaleVersion: String by project

dependencies {
    implementation(gradleApi())
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:utils"))
    implementation("org.funktionale:funktionale-try:$funktionaleVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    // TODO: Use https://github.com/docker-java/docker-java
    implementation("de.gesellix:docker-client:2019-11-26T12-39-35")
}
