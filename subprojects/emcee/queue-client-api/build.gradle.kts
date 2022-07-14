plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.ksp")
}

dependencies {
    api(projects.subprojects.emcee.queueApiModels)
    api(libs.retrofit)

    implementation(libs.moshi)
    implementation(libs.moshiRetrofit)

    ksp(libs.moshiCodegen)
}
