plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(Dependencies.statsd)
    implementation(project(":subprojects:common:logger"))
}
