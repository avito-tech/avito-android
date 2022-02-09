plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    kotlin("kapt") // TODO replace with ksp
}

dependencies {
    api(projects.subprojects.emcee.queueClientModels)
    implementation(libs.moshi)
    kapt(libs.moshiKapt)
}
