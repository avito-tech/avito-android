plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":common:graphite"))

    implementation(gradleApi())
    implementation(project(":gradle:utils"))
    implementation(project(":gradle:logging"))
    implementation(project(":gradle:kotlin-dsl-support"))
}
