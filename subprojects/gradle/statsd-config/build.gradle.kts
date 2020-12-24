plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":common:statsd"))

    implementation(gradleApi())
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":gradle:gradle-extensions"))
}
