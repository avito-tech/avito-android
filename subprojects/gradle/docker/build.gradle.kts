plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(gradleApi())
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.kotlinReflect)
    // TODO: Use https://github.com/docker-java/docker-java
    implementation(Dependencies.dockerClient)
}
