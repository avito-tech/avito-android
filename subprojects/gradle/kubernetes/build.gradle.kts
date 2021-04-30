plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    api(libs.kubernetesClient)
    api(libs.kubernetesDsl)
    // because kubernetes dsl uses old version
    api(libs.kotlinReflect)
    api(project(":gradle:gradle-extensions"))
    api(project(":common:http-client"))

    implementation(gradleApi())
    implementation(libs.officialKubernetesClient)
    implementation(libs.googleAuthLibrary)
    implementation(project(":common:result"))
}
