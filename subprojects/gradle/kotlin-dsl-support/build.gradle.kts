plugins {
    id("kotlin")
    `maven-publish`
}

dependencies {
    implementation(gradleApi())

    testImplementation(project(":subprojects:gradle:test-project"))
}
