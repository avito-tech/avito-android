plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.kubernetesClient)
    api(Dependencies.kubernetesDsl)
    // because kubernetes dsl uses old version
    api(Dependencies.kotlinReflect)
    api(project(":subprojects:gradle:kotlin-dsl-support"))

    implementation(gradleApi())
}
