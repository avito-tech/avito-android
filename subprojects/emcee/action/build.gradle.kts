plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    kotlin("kapt") // TODO replace with ksp
}

dependencies {
    api(projects.subprojects.emcee.shared)
    implementation(projects.subprojects.emcee.queueBackendApi)
    implementation(projects.subprojects.testRunner.instrumentationTestsDexLoader)
    implementation(projects.subprojects.testRunner.testAnnotations)
    implementation(libs.coroutinesCore)
    implementation(libs.moshiKotlin)
    implementation(libs.moshiAdapters)
    implementation(libs.kotlinReflect) {
        because("moshi 1.12.0 depend on 1.4.x kotlin, and 1.13 on 1.6.x, we use 1.5")
    }

    kapt(libs.moshiKapt)
}
