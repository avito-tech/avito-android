plugins {
    id("convention.kotlin-jvm-android")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(libs.truth)
    api(project(":subprojects:common:result"))
}
