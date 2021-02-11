plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

extra["artifact-id"] = "runner-stub"

dependencies {
    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:gradle:runner:service"))
    implementation(project(":subprojects:gradle:runner:shared"))
}
