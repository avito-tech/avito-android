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
    api(project(":subprojects:gradle:gradle-extensions"))

    implementation(gradleApi())
    implementation(Dependencies.officialKubernetesClient)
    implementation(Dependencies.googleAuthLibrary)
}
