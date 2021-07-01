plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(libs.kubernetesClient)
    api(libs.kubernetesDsl)
    api(libs.kotlinReflect) {
        because("kubernetes dsl uses old version")
    }
    api(project(":gradle:gradle-extensions"))
    api(project(":common:http-client"))

    implementation(gradleApi())
    implementation(libs.officialKubernetesClient)
    implementation(libs.googleAuthLibrary)
    implementation(project(":common:result"))

    testImplementation(project(":common:truth-extensions"))
}
