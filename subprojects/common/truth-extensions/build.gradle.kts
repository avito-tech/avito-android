plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(libs.truth)
    api(project(":subprojects:common:result"))
}
