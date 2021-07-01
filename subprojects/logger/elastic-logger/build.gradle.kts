plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(project(":logger:logger"))
    api(project(":common:elastic"))
}
