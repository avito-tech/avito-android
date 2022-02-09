plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    kotlin("kapt") // TODO replace with ksp
}

dependencies {
    api(projects.subprojects.emcee.queueApiModels)
    implementation(libs.retrofit)
    implementation(libs.moshi)
    implementation(libs.moshiSealedRuntime)

    kapt(libs.moshiSealedCodegen)
    kapt(libs.moshiKapt)
}
