plugins {
    id("convention.kotlin-jvm")
}

dependencies {
    api(project(":subprojects:gradle:artifactory-app-backup"))

    implementation(project(":subprojects:common:test-okhttp"))
}
