plugins {
    id("kotlin")
}

dependencies {
    api(project(":subprojects:common:time"))

    implementation(project(":subprojects:common:logger-test-fixtures"))
}
