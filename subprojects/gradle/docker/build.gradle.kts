plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-kotlin-library")
    id("com.avito.android.libraries")
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
