plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-kotlin-library")
    id("com.avito.android.libraries")
}

dependencies {
    api(libs.kubernetesClient)
    api(libs.kubernetesDsl)
    // because kubernetes dsl uses old version
    api(libs.kotlinReflect)
    api(project(":subprojects:gradle:gradle-extensions"))

    implementation(gradleApi())
    implementation(libs.officialKubernetesClient)
    implementation(libs.googleAuthLibrary)
}
