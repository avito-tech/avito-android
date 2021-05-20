plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    api(project(":logger:logger"))
    api(project(":common:elastic"))
}
