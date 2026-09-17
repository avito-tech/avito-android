plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(project(":subprojects:test-runner:report"))
    api(project(":subprojects:common:result"))
}
