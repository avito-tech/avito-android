plugins {
    id("kotlin")
}

dependencies {
    api(project(":gradle:artifactory-app-backup"))

    implementation(project(":common:test-okhttp"))
}
