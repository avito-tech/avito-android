plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.ksp")
}

dependencies {
    api(projects.subprojects.emcee.queueApiModels)
    api(libs.retrofit)

    implementation(libs.moshi)
    implementation(libs.moshiSealedRuntime)
    implementation(libs.moshiRetrofit)
    implementation(projects.subprojects.emcee.moshiAdapters)
    implementation(projects.subprojects.common.retrofitResultAdapter)

    ksp(libs.moshiCodegen)
    ksp(libs.moshiSealedCodegen)
}
