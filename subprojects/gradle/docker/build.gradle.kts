plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    implementation(gradleApi())
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(libs.funktionaleTry)
    implementation(libs.kotlinReflect)
    // TODO: Use https://github.com/docker-java/docker-java
    implementation(libs.dockerClient) {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
    }
}
