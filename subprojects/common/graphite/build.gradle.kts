plugins {
    id("kotlin")
    `maven-publish`
    id("java-test-fixtures")
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":subprojects:common:logger"))

    testFixturesImplementation(Dependencies.kotlinStdlib)
}
