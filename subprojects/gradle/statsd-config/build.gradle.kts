plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":subprojects:common:statsd"))

    implementation(gradleApi())
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
}
