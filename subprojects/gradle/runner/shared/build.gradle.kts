plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

extra["artifact-id"] = "runner-shared"

dependencies {
    compileOnly(gradleApi())
    api(project(":subprojects:common:logger"))
    implementation(Dependencies.rxJava)
}
