plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":common:math"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:gradle-logger"))
    implementation(gradleApi())
    implementation(Dependencies.googlePublish)
}
