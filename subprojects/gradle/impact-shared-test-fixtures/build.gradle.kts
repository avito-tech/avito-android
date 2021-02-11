plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(gradleApi())
    implementation(project(":subprojects:gradle:impact-shared"))
    implementation(project(":subprojects:gradle:test-project"))
    implementation(Dependencies.Test.truth)
}
