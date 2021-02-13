plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    implementation(libs.junit)
    implementation(libs.truth)
    implementation(libs.hamcrestLib)

    testImplementation(libs.kotlinReflect)
}
