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
    implementation(libs.kotlinReflect) {
        because("moshi 1.12.0 depend on 1.4.x kotlin, and 1.13 on 1.6.x, we use 1.5")
    }

    kapt(libs.moshiKapt)
}
