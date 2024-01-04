plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(project(":subprojects:common:series"))
    api(project(":subprojects:logger:logger"))
}
