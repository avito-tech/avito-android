plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(gradleApi())

    testImplementation(project(":subprojects:gradle:test-project"))
}
