plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(gradleApi())
    implementation(project(":gradle:impact-shared"))
    implementation(project(":gradle:test-project"))
    implementation(Dependencies.Test.truth)
}
