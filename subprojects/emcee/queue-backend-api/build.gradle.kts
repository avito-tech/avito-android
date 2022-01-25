plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    kotlin("kapt") // TODO replace with ksp
}

dependencies {
    api(projects.subprojects.emcee.shared)
    api(libs.retrofit)
    implementation(libs.moshiKotlin)
    implementation(libs.moshiRetrofit)
    kapt(libs.moshiKapt)
}
