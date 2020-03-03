plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

val statsdVersion: String by project

dependencies {
    api(project(":subprojects:common:graphite"))

    implementation(gradleApi())
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
}
