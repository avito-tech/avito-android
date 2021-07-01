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
    api(projects.gradle.gradleExtensions)
    api(projects.common.httpClient)

    implementation(gradleApi())
    implementation(libs.officialKubernetesClient)
    implementation(libs.googleAuthLibrary)
    implementation(projects.common.result)

    testImplementation(projects.common.truthExtensions)
}
