plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(libs.kubernetesClient)
    api(libs.kubernetesDsl)
    // because kubernetes dsl uses old version
    api(libs.kotlinReflect)
    api(project(":gradle:gradle-extensions"))

    implementation(gradleApi())
    implementation(libs.officialKubernetesClient)
    implementation(libs.googleAuthLibrary)
    implementation(libs.kotlinStdlib)
}
