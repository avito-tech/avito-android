plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":common:graphite"))

    implementation(gradleApi())
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":gradle:gradle-extensions"))
}
