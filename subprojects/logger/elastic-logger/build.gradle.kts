plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(project(":subprojects:logger:logger"))
    api(project(":subprojects:common:elastic"))
}
