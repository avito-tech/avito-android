plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

extra["artifact-id"] = "runner-shared"

dependencies {
    compileOnly(gradleApi())
    api(project(":common:logger"))
    implementation(Dependencies.rxJava)
}
