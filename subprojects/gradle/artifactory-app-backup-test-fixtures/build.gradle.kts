plugins {
    id("kotlin")
}

dependencies {
    api(project(":subprojects:gradle:artifactory-app-backup"))

    implementation(project(":subprojects:common:test-okhttp"))
}
