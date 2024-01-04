plugins {
    id("convention.kotlin-jvm-android")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(project(":subprojects:test-runner:report"))
    api(project(":subprojects:common:result"))
}
