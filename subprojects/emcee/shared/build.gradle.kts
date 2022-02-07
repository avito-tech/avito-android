plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    kotlin("kapt") // TODO replace with ksp
}

dependencies {
    implementation(libs.moshiKotlin)
    implementation(libs.kotlinReflect) {
        because("moshi 1.12.0 depend on 1.4.x kotlin, and 1.13 on 1.6.x, we use 1.5")
    }
    implementation(libs.moshiSealedRuntime)

    kapt(libs.moshiKapt)
    kapt(libs.moshiSealedCodegen)
}
