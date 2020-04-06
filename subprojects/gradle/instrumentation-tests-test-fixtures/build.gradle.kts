plugins {
    id("kotlin")
}

dependencies {
    api(project(":subprojects:gradle:instrumentation-tests"))

    implementation(project(":subprojects:gradle:bitbucket"))
    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:gradle:slack"))
    implementation(project(":subprojects:gradle:statsd-config"))
    implementation(project(":subprojects:gradle:kubernetes"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:test-project"))
}
