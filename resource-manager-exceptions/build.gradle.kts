plugins {
    id("kotlin")
    `maven-publish`
}

dependencies {
    implementation(project(":report-viewer"))
}
