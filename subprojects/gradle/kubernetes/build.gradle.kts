plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    // TODO: describe reasons why don't we use an official client https://github.com/kubernetes-client/java
    api(Dependencies.kubernetesClient)
    api(Dependencies.kubernetesDsl)
    // because kubernetes dsl uses old version
    api(Dependencies.kotlinReflect)

    implementation(gradleApi())
}
