plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.ksp")
}

dependencies {
    implementation(libs.moshi)
    ksp(libs.moshiCodegen)
}
