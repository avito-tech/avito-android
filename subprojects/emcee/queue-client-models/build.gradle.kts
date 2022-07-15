plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.ksp")
}

dependencies {
    implementation(libs.moshi)
    implementation(libs.moshiSealedRuntime)

    ksp(libs.moshiCodegen)
    ksp(libs.moshiSealedCodegen)
}
