plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.ksp")
}

dependencies {
    api(projects.subprojects.emcee.queueClientModels)

    implementation(libs.moshi)

    ksp(libs.moshiCodegen)
}
