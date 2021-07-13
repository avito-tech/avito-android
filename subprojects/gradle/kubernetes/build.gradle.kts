plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
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
    implementation(libs.coroutinesCore)
    implementation(projects.common.result)
    implementation(projects.common.waiter)
    implementation(projects.logger.logger)

    testImplementation(projects.common.truthExtensions)
}
