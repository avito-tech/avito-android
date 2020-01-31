plugins {
    id("kotlin")
    `maven-publish`
}

dependencies {
    implementation(project(":subprojects:common:report-viewer"))
}
