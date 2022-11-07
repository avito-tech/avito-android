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
    api(projects.subprojects.gradle.gradleExtensions)
    api(projects.subprojects.common.httpStatsd)

    compileOnly(gradleApi())
    implementation(libs.officialKubernetesClient)
    implementation(libs.googleAuthLibrary)
    implementation(libs.coroutinesCore)
    implementation(projects.subprojects.common.result)
    implementation(projects.subprojects.common.waiter)

    testImplementation(projects.subprojects.common.truthExtensions)
}
