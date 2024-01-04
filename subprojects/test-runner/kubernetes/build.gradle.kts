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
    api(project(":subprojects:gradle:gradle-extensions"))
    api(project(":subprojects:common:http-statsd"))

    compileOnly(gradleApi())
    implementation(libs.officialKubernetesClient)
    implementation(libs.googleAuthLibrary)
    implementation(libs.coroutinesCore)
    implementation(project(":subprojects:common:result"))
    implementation(project(":subprojects:common:waiter"))

    testImplementation(project(":subprojects:common:truth-extensions"))
}
