plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
    `java-test-fixtures`
}

dependencies {
    api(project(":subprojects:common:logger"))
}
