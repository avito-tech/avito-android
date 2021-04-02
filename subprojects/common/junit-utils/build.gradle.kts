plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    implementation(libs.kotlinStdlib)
    implementation(libs.junit)
    implementation(libs.truth)
    implementation(libs.hamcrestLib)

    testImplementation(libs.kotlinReflect)
}
