plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

extra["artifact-id"] = "runner-stub"

dependencies {
    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:gradle:runner:service"))
    implementation(project(":subprojects:gradle:runner:shared"))
}
