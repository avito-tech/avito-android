plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    kotlin("kapt") // TODO replace with ksp
}

dependencies {
    api(projects.subprojects.emcee.queueApiModels)
    api(libs.retrofit)

    implementation(libs.moshi)
    implementation(libs.moshiRetrofit)

    kapt(libs.moshiKapt)
}
