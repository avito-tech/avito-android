plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(project(":subprojects:test-runner:report-viewer"))

    implementation(gradleApi())
    api(project(":subprojects:gradle:build-environment"))
    api(project(":subprojects:gradle:git"))
}
