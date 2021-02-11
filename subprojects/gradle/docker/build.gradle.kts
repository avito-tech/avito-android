plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(gradleApi())
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.kotlinReflect)
    // TODO: Use https://github.com/docker-java/docker-java
    implementation(Dependencies.dockerClient) {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
    }
}
