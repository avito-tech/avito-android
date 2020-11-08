plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":common:statsd"))

    implementation(gradleApi())
    implementation(project(":gradle:ci-logger"))
    implementation(project(":gradle:kotlin-dsl-support"))
}
