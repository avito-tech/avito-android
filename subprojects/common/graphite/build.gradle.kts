plugins {
    id("kotlin")
    `maven-publish`
    id("java-test-fixtures")
    id("com.jfrog.bintray")
}

val kotlinVersion: String by project

dependencies {
    api(project(":subprojects:common:logger"))

    testFixturesImplementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}
