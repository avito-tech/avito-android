plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    `java-test-fixtures`
}

dependencies {
    api(project(":subprojects:common:logger"))
}
