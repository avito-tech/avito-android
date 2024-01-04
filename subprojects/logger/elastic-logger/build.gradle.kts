plugins {
    id("convention.kotlin-jvm-android")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(project(":subprojects:logger:logger"))
    api(project(":subprojects:common:elastic"))
}
